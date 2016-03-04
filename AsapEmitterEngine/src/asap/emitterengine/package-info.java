/*******************************************************************************
 *******************************************************************************/
/**
The Emitter Engine gives us BML level control over Emitters.
<p>An Emitter will autonomously, possibly on its own thread, generate and send to a Realizer little blocks of BML behavior, such
as for eye blinks.
An Emitter Engine processes BML behaviors that allow one to:
<ul>
  <li> create a new emitter of the type associated with this engine
  <li> set parameters for the emitter
</ul>

<p>This package provides the interfaces and default components, and one example implementation: the BlinkEmitter engine.
When you want to make a new Emitter engine, e.g., for random gaze behaviors, you can easily emulate the BlinkEmitter 
engine example.

<h2>BML Behaviors</h2>
Behavior that creates an emitter; emitter operates as long as behavior is not ended.  
<i>Must be subclassed for new type of emitter engine, to create proper type of emitter, 
and to map new parameters. But maybe we can use templating: CreateEmitterBehavior &lt;BlinkEmitter&gt;, 
where BlinkEmitter also should specify the xml tag...</i><br>
<code>&lt;bml id="bmle1"&gt;&lt;bmlt:blinkemitter id="be1" start="0" range="2" averagewaitingtime="5"/&gt;&lt;/bml&gt;</code><br>
<br>
Behavior to change the parameter of a given emitter: <i>Reused for all types of emitter engine</i><br>
<code>
&lt;bmlt:emitterparametervaluechange id="epvc1" target="bml11:be1" paramId="averagewaitingtime" start="3"&gt;<br>
&nbsp;&nbsp;&lt;bmlt:trajectory type="instant" targetValue="3"/&gt;<br>
&lt;/bmlt:parametervaluechange&gt;<br>
</code><br>
<br>
Behavior to stop existing emitter: Simply set an end time, or interrupt the original behavior; when the original emitter 
behavior ends, the emitter will be stopped and cleaned up.

<h2>Plan units</h2>
<ul>
<li>A plan unit that creates a given emitter type when it is started, and stops and destroys it when the plan unit ends. 
<i>needs not be subclassed; you use templating to make CreateEmitterUnit &lt;BlinkEmitter&gt;</i>
<li>A plan unit for emitterparametervaluechanges, based upon the parametervaluechange. 
If persistent behavior (no end time specified), it ends as soon as change has been made (cf activate behavior). 
This means that as a persistent behavior, it only makes sense to use it with a instant trajectory. If not persistent, 
it ends when end time reached. <i>Reused for all types of emitter engine</i>
</ul>

<h2>Planners and players</h2>
Planners and Players... anything special here?
The Engine could be templated...
EmmiterEngine&lt;?extends EmitBehavior,?extends TimedEmitUnit&gt;
 */
package asap.emitterengine;
