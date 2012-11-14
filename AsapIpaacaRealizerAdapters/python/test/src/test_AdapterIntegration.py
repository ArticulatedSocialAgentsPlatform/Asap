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
        self.bmlToIpaaca = BMLRealizerToIpaacaAdapter()
        self.ipaacaToBML = IpaacaToBMLRealizerAdapter(self.mockRealizerPort)
        
    def tearDown(self):
        pass
    def testPerformBML(self):
        self.bmlToIpaaca.performBML("bmltest")
        time.sleep(0.05)
        verify(self.mockRealizerPort).performBML("bmltest")
    def testFeedback(self):
        self.bmlToIpaaca.addListeners(self.mockFeedbackListener);
        self.ipaacaToBML.feedback("bmlfeedback");
        time.sleep(0.05)
        verify(self.mockFeedbackListener).feedback("bmlfeedback");    


if __name__ == "__main__":
    unittest.main()