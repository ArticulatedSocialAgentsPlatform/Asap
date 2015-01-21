'''
Created on Nov 5, 2012
Submits ipaaca messages (from an InputBuffer) to a RealizerPort; submits RealizerPort feedbacks to the OutputBuffer. 
Assumes that the connected realizerport is threadsafe (or at least that its performBML function is).
@author: hvanwelbergen
'''
from asap_realizerport.BMLFeedbackListener import BMLFeedbackListener 
from ipaaca import InputBuffer, IUEventType
from ipaaca import OutputBuffer
from ipaaca import Message
import IpaacaBMLConstants

class IpaacaToBMLRealizerAdapter(BMLFeedbackListener):
    '''
    classdocs
    '''

    def __init__(self, realizerPort, characterId="default"):
        '''
        Constructor
        '''
        self.realizerPort = realizerPort
        self.realizerPort.addListeners(self)
        self.inBuffer = InputBuffer("IpaacaToBMLRealizerAdapter",[IpaacaBMLConstants.BML_CATEGORY], characterId)
        self.outBuffer = OutputBuffer("IpaacaToBMLRealizerAdapter", characterId)
        self.inBuffer.register_handler(self.handle_iu_event, [IUEventType.MESSAGE], IpaacaBMLConstants.BML_CATEGORY)
    
    def handle_iu_event(self, iu, event_type, local):
        self.realizerPort.performBML(iu.payload[IpaacaBMLConstants.BML_KEY])
        return
    
    def feedback(self, feedback):
        feedbackIU = Message(IpaacaBMLConstants.BML_FEEDBACK_CATEGORY)        
        feedbackIU.payload[IpaacaBMLConstants.BML_FEEDBACK_KEY]=feedback
        self.outBuffer.add(feedbackIU)
        return
