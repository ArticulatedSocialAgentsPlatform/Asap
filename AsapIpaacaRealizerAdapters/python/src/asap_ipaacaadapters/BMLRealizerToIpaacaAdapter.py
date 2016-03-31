'''
Created on Nov 5, 2012

@author: hvanwelbergen
'''
from asap_realizerport.RealizerPort import RealizerPort
from asap_ipaacaadapters import IpaacaBMLConstants
from ipaaca import InputBuffer, IUEventType, OutputBuffer, Message

        
class BMLRealizerToIpaacaAdapter(RealizerPort):
    '''
    classdocs
    '''


    def __init__(self, characterId="default"):
        '''
        Constructor
        '''
        self.inBuffer = InputBuffer("BMLToIpaacaRealizerAdapter", [IpaacaBMLConstants.BML_FEEDBACK_CATEGORY], characterId)
        self.outBuffer = OutputBuffer("BMLToIpaacaRealizerAdapter", characterId)
        self.feedbackListeners = []
        self.inBuffer.register_handler(self.handle_iu_event, [IUEventType.MESSAGE], IpaacaBMLConstants.BML_FEEDBACK_CATEGORY)
    
    def handle_iu_event(self, iu, event_type, local):
        for ls in self.feedbackListeners:
            ls.feedback(iu.payload[IpaacaBMLConstants.BML_FEEDBACK_KEY])
                    
    def addListeners(self, *listeners):
        self.feedbackListeners.extend(list(listeners))
    
    def removeAllListeners(self):
        self.feedbackListeners = []
        
    def performBML(self, bmlString):
        message = Message(IpaacaBMLConstants.BML_CATEGORY)
        message.payload[IpaacaBMLConstants.BML_KEY] = bmlString
        self.outBuffer.add(message)
