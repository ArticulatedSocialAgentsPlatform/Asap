import abc
'''
Created on Nov 5, 2012

@author: hvanwelbergen
'''

class BMLFeedbackListener(object):
    '''
    classdocs
    '''


    def __init__(self):
        '''
        Constructor        
        '''
    
    @abc.abstractmethod        
    def feedback(self, feedback):
        return