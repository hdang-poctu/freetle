/*
* Copyright 2010-2011 Weigle Wilczek GmbH
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.freetle.util

import org.apache.log4j.spi.LocationInfo
import java.lang.String


/**
* Mixin providing a Logger for the type mixed into.
*/
trait LoggingWithConstructorLocation {
  val info: String
  /**
* Logger for the type mixed into.
*/
  protected[util] lazy val logger : Logger = {


    //System.out.println()
    //exception.printStackTrace()
    //System.out.println("name : "+name)

    Logger(this.getClass, info)
  }
}

