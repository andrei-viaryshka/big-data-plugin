/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.big.data.plugins.common.ui;

import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.big.data.api.cluster.NamedCluster;
import org.pentaho.big.data.api.cluster.NamedClusterService;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.pentaho.runtime.test.RuntimeTester;
import org.pentaho.runtime.test.action.RuntimeTestActionService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.pentaho.big.data.plugins.common.ui.HadoopClusterDelegateImpl.PKG;
import static org.pentaho.big.data.plugins.common.ui.HadoopClusterDelegateImpl
  .SPOON_DIALOG_ERROR_SAVING_NAMED_CLUSTER_MESSAGE;
import static org.pentaho.big.data.plugins.common.ui.HadoopClusterDelegateImpl
  .SPOON_DIALOG_ERROR_SAVING_NAMED_CLUSTER_TITLE;

/**
 * Created by bryan on 10/19/15.
 */
public class HadoopClusterDelegateImplTest {
  private Spoon spoon;
  private NamedClusterService namedClusterService;
  private RuntimeTestActionService runtimeTestActionService;
  private RuntimeTester runtimeTester;
  private HadoopClusterDelegateImpl hadoopClusterDelegate;
  private IMetaStore metaStore;
  private NamedCluster namedCluster;
  private String namedClusterName;
  private CommonDialogFactory commonDialogFactory;
  private Shell shell;
  private VariableSpace variables;

  @Before
  public void setup() {
    spoon = mock( Spoon.class );
    shell = mock( Shell.class );
    when( spoon.getShell() ).thenReturn( shell );
    namedClusterService = mock( NamedClusterService.class );
    runtimeTestActionService = mock( RuntimeTestActionService.class );
    runtimeTester = mock( RuntimeTester.class );
    metaStore = mock( IMetaStore.class );
    namedCluster = mock( NamedCluster.class );
    variables = new Variables();
    namedClusterName = "namedClusterName";
    when( namedCluster.getName() ).thenReturn( namedClusterName );
    commonDialogFactory = mock( CommonDialogFactory.class );
    hadoopClusterDelegate =
      new HadoopClusterDelegateImpl( spoon, namedClusterService, runtimeTestActionService, runtimeTester,
        commonDialogFactory );
  }

  @Test
  public void testSimpleConstructor() {
    assertNotNull(
      new HadoopClusterDelegateImpl( spoon, namedClusterService, runtimeTestActionService, runtimeTester ) );
  }

  @Test
  public void testDupeNamedClusterNullNc() {
    hadoopClusterDelegate.dupeNamedCluster( metaStore, null, shell );
    verifyNoMoreInteractions( metaStore, shell );
  }

  @Test
  public void testDupeNamedClusterNullNewName() {
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    NamedCluster clonedNamedCluster = mock( NamedCluster.class );
    when( namedCluster.clone() ).thenReturn( clonedNamedCluster );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        clonedNamedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( null );

    hadoopClusterDelegate.dupeNamedCluster( metaStore, namedCluster, shell );

    verify( namedClusterDialog ).setNewClusterCheck( true );
    verify( clonedNamedCluster ).setName(
      BaseMessages.getString( Spoon.class, HadoopClusterDelegateImpl.SPOON_VARIOUS_DUPE_NAME ) + namedClusterName );
    verifyNoMoreInteractions( metaStore );
  }

  @Test
  public void testDupeNamedClusterNullMetastore() throws MetaStoreException {
    String newName = "newName";
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    NamedCluster clonedNamedCluster = mock( NamedCluster.class );
    DelegatingMetaStore spoonMetastore = mock( DelegatingMetaStore.class );
    when( spoon.getMetaStore() ).thenReturn( spoonMetastore );
    when( namedCluster.clone() ).thenReturn( clonedNamedCluster );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        clonedNamedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( newName );

    hadoopClusterDelegate.dupeNamedCluster( null, namedCluster, shell );

    verify( namedClusterDialog ).setNewClusterCheck( true );
    verify( clonedNamedCluster ).setName(
      BaseMessages.getString( Spoon.class, HadoopClusterDelegateImpl.SPOON_VARIOUS_DUPE_NAME ) + namedClusterName );
    verify( namedClusterService ).create( clonedNamedCluster, spoonMetastore );
    verify( spoon ).refreshTree();
  }

  @Test
  public void testDelNamedCluster() throws MetaStoreException {
    when( namedClusterService.read( namedClusterName, metaStore ) ).thenReturn( namedCluster );
    hadoopClusterDelegate.delNamedCluster( metaStore, namedCluster );
    verify( namedClusterService ).delete( namedClusterName, metaStore );
    verify( spoon ).refreshTree();
    verify( spoon ).setShellText();
  }

  @Test
  public void testDelNamedClusterNull() throws MetaStoreException {
    when( namedClusterService.read( namedClusterName, metaStore ) ).thenReturn( null );
    hadoopClusterDelegate.delNamedCluster( metaStore, namedCluster );
    verify( namedClusterService, never() ).delete( namedClusterName, metaStore );
    verify( spoon ).refreshTree();
    verify( spoon ).setShellText();
  }

  @Test
  public void testDelNamedClusterNullMetastore() throws MetaStoreException {
    DelegatingMetaStore metaStore2 = mock( DelegatingMetaStore.class );
    when( spoon.getMetaStore() ).thenReturn( metaStore2 );
    when( namedClusterService.read( namedClusterName, metaStore2 ) ).thenReturn( namedCluster );
    hadoopClusterDelegate.delNamedCluster( null, namedCluster );
    verify( namedClusterService ).delete( namedClusterName, metaStore2 );
    verify( spoon ).refreshTree();
    verify( spoon ).setShellText();
  }

  @Test
  public void testDelNamedClusterException() throws MetaStoreException {
    when( namedClusterService.read( namedClusterName, metaStore ) ).thenReturn( namedCluster );
    MetaStoreException metaStoreException = new MetaStoreException();
    doThrow( metaStoreException ).when( namedClusterService ).delete( namedClusterName, metaStore );
    hadoopClusterDelegate.delNamedCluster( metaStore, namedCluster );
    verify( commonDialogFactory ).createErrorDialog( shell, BaseMessages.getString( PKG,
        HadoopClusterDelegateImpl.SPOON_DIALOG_ERROR_DELETING_NAMED_CLUSTER_TITLE ), BaseMessages
        .getString( PKG,
          HadoopClusterDelegateImpl.SPOON_DIALOG_ERROR_DELETING_NAMED_CLUSTER_MESSAGE, namedClusterName ),
      metaStoreException );
    verify( spoon ).refreshTree();
    verify( spoon ).setShellText();
  }

  @Test
  public void testEditNamedClusterNullMetastore() throws MetaStoreException {
    DelegatingMetaStore spoonMetastore = mock( DelegatingMetaStore.class );
    when( spoon.getMetaStore() ).thenReturn( spoonMetastore );

    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    NamedCluster clonedNamedCluster = mock( NamedCluster.class );
    when( namedClusterService.read( namedClusterName, spoonMetastore ) ).thenReturn( namedCluster );
    when( namedCluster.clone() ).thenReturn( clonedNamedCluster );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        clonedNamedCluster ) ).thenReturn( namedClusterDialog );
    String clonedName = "clonedName";
    when( clonedNamedCluster.getName() ).thenReturn( clonedName );
    when( namedClusterDialog.open() ).thenReturn( clonedName );
    when( namedClusterDialog.getNamedCluster() ).thenReturn( clonedNamedCluster );

    assertEquals( clonedName, hadoopClusterDelegate.editNamedCluster( null, namedCluster, shell ) );

    verify( namedClusterDialog ).setNewClusterCheck( false );
    verify( spoon ).refreshTree();
    verify( namedClusterService ).delete( namedClusterName, spoonMetastore );
    verify( namedClusterService ).create( clonedNamedCluster, spoonMetastore );
  }

  @Test
  public void testEditNamedClusterNull() throws MetaStoreException {
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    NamedCluster clonedNamedCluster = mock( NamedCluster.class );
    when( namedClusterService.read( namedClusterName, metaStore ) ).thenReturn( namedCluster );
    when( namedCluster.clone() ).thenReturn( clonedNamedCluster );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        clonedNamedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( null );

    hadoopClusterDelegate.editNamedCluster( metaStore, namedCluster, shell );

    verify( namedClusterDialog ).setNewClusterCheck( false );
    verifyNoMoreInteractions( namedClusterService );
  }

  @Test
  public void testNewNamedClusterNullMetastore() throws MetaStoreException {
    DelegatingMetaStore spoonMetastore = mock( DelegatingMetaStore.class );
    when( spoon.getMetaStore() ).thenReturn( spoonMetastore );

    when( namedClusterService.getClusterTemplate() ).thenReturn( namedCluster );
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        namedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( namedClusterName );

    assertEquals( namedClusterName, hadoopClusterDelegate.newNamedCluster( variables, null, shell ) );

    verify( namedClusterDialog ).setNewClusterCheck( true );
    verify( namedCluster ).shareVariablesWith( variables );
    verify( namedClusterService ).create( namedCluster, spoonMetastore );
    verify( spoon ).refreshTree();
  }

  @Test
  public void testNewNamedClusterNullVariables() throws MetaStoreException {
    when( namedClusterService.getClusterTemplate() ).thenReturn( namedCluster );
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        namedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( namedClusterName );

    assertEquals( namedClusterName, hadoopClusterDelegate.newNamedCluster( null, metaStore, shell ) );

    verify( namedClusterDialog ).setNewClusterCheck( true );
    verify( namedCluster ).initializeVariablesFrom( null );
    verify( namedClusterService ).create( namedCluster, metaStore );
    verify( spoon ).refreshTree();
  }

  @Test
  public void testNewNamedClusterNullResult() throws MetaStoreException {
    when( namedClusterService.getClusterTemplate() ).thenReturn( namedCluster );
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        namedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( null );

    assertNull( hadoopClusterDelegate.newNamedCluster( null, metaStore, shell ) );

    verify( namedClusterDialog ).setNewClusterCheck( true );
    verify( namedClusterService, times( 0 ) ).create( any( NamedCluster.class ), any( IMetaStore.class ) );
    verify( spoon, times( 0 ) ).refreshTree();
  }

  @Test
  public void testNewNamedClusterErrorSaving() throws MetaStoreException {
    when( namedClusterService.getClusterTemplate() ).thenReturn( namedCluster );
    NamedClusterDialogImpl namedClusterDialog = mock( NamedClusterDialogImpl.class );
    when( commonDialogFactory
      .createNamedClusterDialog( shell, namedClusterService, runtimeTestActionService, runtimeTester,
        namedCluster ) ).thenReturn( namedClusterDialog );
    when( namedClusterDialog.open() ).thenReturn( namedClusterName );
    MetaStoreException metaStoreException = new MetaStoreException();
    doThrow( metaStoreException ).when( namedClusterService ).create( namedCluster, metaStore );

    hadoopClusterDelegate.newNamedCluster( variables, metaStore, shell );

    verify( commonDialogFactory ).createErrorDialog( shell,
      BaseMessages.getString( PKG, SPOON_DIALOG_ERROR_SAVING_NAMED_CLUSTER_TITLE ),
      BaseMessages.getString( PKG, SPOON_DIALOG_ERROR_SAVING_NAMED_CLUSTER_MESSAGE, namedCluster.getName() ),
      metaStoreException );
  }
}
