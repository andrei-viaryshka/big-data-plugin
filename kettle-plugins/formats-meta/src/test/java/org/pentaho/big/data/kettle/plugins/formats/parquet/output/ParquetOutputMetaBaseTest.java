/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.big.data.kettle.plugins.formats.parquet.output;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class ParquetOutputMetaBaseTest {

  @Mock StepMeta parentStepMeta;
  @Mock TransMeta parentTransMeta;
  @Mock NamedClusterEmbedManager namedClusterEmbedManager;
  private ParquetOutputMetaBase metaBase;

  @Before
  public void setUp() throws Exception {
    metaBase = spy( new ParquetOutputMetaBase() {
      @Override
      public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
                                    TransMeta transMeta,
                                    Trans trans ) {
        return null;
      }

      @Override public StepDataInterface getStepData() {
        return null;
      }
    } );
  }

  @Test
  public void getXMLShouldCallRegisterUrl() {
    metaBase.setFilename( "hc://HC/fileName" );
    when( parentStepMeta.getParentTransMeta() ).thenReturn( parentTransMeta );
    when( parentTransMeta.getNamedClusterEmbedManager() ).thenReturn( namedClusterEmbedManager );
    metaBase.setParentStepMeta( parentStepMeta );

    metaBase.getXML();
    verify( namedClusterEmbedManager ).registerUrl( eq( "hc://HC/fileName" ) );
  }
}
