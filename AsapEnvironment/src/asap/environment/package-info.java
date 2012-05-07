/*******************************************************************************
 * 
 * Copyright (C) 2009 Human Media Interaction, University of Twente, the Netherlands
 * 
 * This file is part of the Elckerlyc BML realizer.
 * 
 * Elckerlyc is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Elckerlyc is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Elckerlyc.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/

/**
 <p>Loading a complete Virtual Human setup, consisting of its Embodiments and its Engines.</p>

 <h1>Virtual Human Loader modules</h1>

 <p>In Elckerlyc, a Virtual Humans setup can be specified by, and loaded from, an XML file that specifies exactly which 
 embodiments and engines must be constructed, plus some additional settings for the BMLRealizer Port and Adapter setup.
 Such a specification may, e.g., specify a combination of a 3D graphical embodiment, an AnimationEngine with a specific 
 custom gesture binding, and a default MARYTTS SpeechEngine, plus a BMLRealizer with a LoggingPipe and a Server.</p>

 <h2>XML format of Loader specifications</h2>

 <p>The XML specification consists of three blocks: one specifying the BMLRealizer setup (Ports and Adapters), one containing 
 a set of Embodiment specifications, and one containing a set of Engine specifications. An Embodiment or Engine entry specifies 
 an ID, and the fully qualified name of a <i>Loader class</i>. The loader class subclasses EngineLoader or EmbodimentLoader, 
 and is responsible for reading the XML content of the entry, constructing the Engine or Embodiment, and adding it to the 
 appropriate environment and/or engine and/or realizer.</p>

 <p>One can include other XML files using the <? include file="..." ?> directive. A set of files specifying default entries 
 (each containing one Embodiment or one Engine) is already available, to facilitate easy recombination of previously used 
 embodiments and engines.</p>

 <h2>Connecting Embodiments and Engines</h2>

 <p>An Engine requires a certain embodiment to operate upon. For example, the MixedAnimationEngine requires a MixedSkeletonEmbodiment 
 to animate, the FaceEngine requires a FaceEmbodiment, etcetera. At loading time, a loader may request to get access to specific 
 embodimentloaders or engineloaders, in order to get access to data from that loader. An EngineLoader may, e.g., request connection 
 to another EngineLoader (such as, e.g., the Speech Engine requiring access to the FaceEngine for playing Visemes)</p>

 <p>At loading time, the system will first load all embodiments, then load all engines. Conenctions are made by the loader.</p>

 <h2>Typical Engines and Embodiments</h2>
 A typical Embodiment implementation will implement a series of Embodiment interfaces. For example, the standard embodiment 
 of an avatar in the HMIRender environment will implement VGLEmbodiment, and VJointEmbodiment, FaceEmbodiment. The TextLabel 
 embodiment used to present speech in the form of text (instead of audio) implements both TextEmbodiment 
 (accessed by the SpeechEngine) and JComponentEmbodiment (and as such, can be added to a GUI Panel as label). 
 Another TextEmbodiment is the StdOutTextEmbodiment, which is NOT a JCOmponent, but renders the text to StdOut.

 <ul>
 <li>SkeletonEmbodiment ("the animatable skeleton") --- provides animation control in a rendering environment, e.g., Ogre, HMIRender, VIT, ...</li>
 <li>FaceEmbodiment ("the animatable face") --- e.g. in HMIRender or XFace</li>
 <li>JComponentEmbodiment (useful for labels and such that can be put on a Panel...) --- e.g., textlabel for Text speech engine</li> 
 <li>AudioEmbodiment (something to stream audio to)</li>
 <li></li>
 </ul>
 <ul>
 <li>AnimationEngine, requires VJointEmbodiment</li>
 <li>FaceEngine, requires FaceEmbodiment</li>
 <li>SpeechEngine, requires AudioEmbodiment or TextLabelEmbodiment as well as FaceEngine and AnimationEngine</li>
 <li>NabaztagEngine, requires NabaztagEmbodiment</li>
 <li>PictureEngine, requires PictureEmbodiment</li>
 </ul>

 */
package asap.environment;