'''
Created on Nov 5, 2012

@author: hvanwelbergen
'''
import unittest
from asap_ipaacaadapters.BMLRealizerToIpaacaAdapter import BMLRealizerToIpaacaAdapter
from asap_ipaacaadapters.IpaacaToBMLRealizerAdapter import IpaacaToBMLRealizerAdapter 
from mockito.mockito import verify
from mockito.mocking import mock 
import time


class AdaptersIntegrationTest(unittest.TestCase):
    def setUp(self):
        self.mockRealizerPort = mock()
        self.mockFeedbackListener = mock()
    def tearDown(self):
        pass
    def testPerformBML(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter()
        IpaacaToBMLRealizerAdapter(self.mockRealizerPort)
        bmlToIpaaca.performBML("bmltest")
        time.sleep(0.05)
        verify(self.mockRealizerPort).performBML("bmltest")
    def testPerformBMLCharacter(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter("Fred")
        IpaacaToBMLRealizerAdapter(self.mockRealizerPort,"Fred")     
        bmlToIpaaca.performBML("bmltest")
        time.sleep(0.05)
        verify(self.mockRealizerPort).performBML("bmltest")
    def testPerformBMLOtherCharacter(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter("Fred")
        IpaacaToBMLRealizerAdapter(self.mockRealizerPort,"Wilma")     
        bmlToIpaaca.performBML("bmltest")
        time.sleep(0.05)
        verify(self.mockRealizerPort,times=0).performBML("bmltest")   
    def testFeedback(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter()
        ipaacaToBML = IpaacaToBMLRealizerAdapter(self.mockRealizerPort)
        bmlToIpaaca.addListeners(self.mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        time.sleep(0.05)
        verify(self.mockFeedbackListener).feedback("bmlfeedback");    
    def testFeedbackCharacter(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter("Fred")
        ipaacaToBML = IpaacaToBMLRealizerAdapter(self.mockRealizerPort,"Fred")
        bmlToIpaaca.addListeners(self.mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        time.sleep(0.05)
        verify(self.mockFeedbackListener).feedback("bmlfeedback");    
    def testFeedbackOtherCharacter(self):
        bmlToIpaaca = BMLRealizerToIpaacaAdapter("Fred")
        ipaacaToBML = IpaacaToBMLRealizerAdapter(self.mockRealizerPort,"Wilma")
        bmlToIpaaca.addListeners(self.mockFeedbackListener);
        ipaacaToBML.feedback("bmlfeedback");
        time.sleep(0.05)
        verify(self.mockFeedbackListener,times=0).feedback("bmlfeedback"); 
        
if __name__ == "__main__":
    unittest.main()