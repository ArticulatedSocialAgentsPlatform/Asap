import abc

'''
Created on Nov 1, 2012

@author: Herwin
'''

class RealizerPort(object):
    '''
    Standarddized interface to a BML (Asap)Realizer
    '''
    __metaclass__ = abc.ABCMeta

    def __init__(self):
        '''
        Constructor
        '''
        
    @abc.abstractmethod
    def addListeners(self, *feedbackListeners):
        '''
        Add some listeners to which BML Feedback will be sent
        '''
        return
    
    @abc.abstractmethod
    def removeAllListeners(self):
        return
    
    @abc.abstractmethod
    def performBML(self, bmlString):
        '''
        Asks the realizer to perform a BML block. Non-blocking: this call will NOT block until the BML 
        has been completely performed! It may block until the BML has been scheduled, though -- this is undetermined.
        '''        
        return