__version__ = '0.10.0'
__commit__ = 'g867672c'

def getVersion():
    """
    Returns a descriptive string for the version of RSB.
    """
    return "%s-%s" % (__version__, __commit__)
