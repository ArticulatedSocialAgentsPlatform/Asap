/*******************************************************************************
 *******************************************************************************/
package asap.tcpipadapters;

/** 
See package documentation.

@author Dennis Reidsma
  */
public class ServerInfo
{
  
  private int bmlPort       = 7500;
  private int feedbackPort  = 7501;
  private String serverName = "127.0.0.1";
  
  public ServerInfo(String serverName, int bmlPort, int feedbackPort)
  {
    this.bmlPort      = bmlPort;
    this.feedbackPort = feedbackPort;
    this.serverName   = serverName;
  }
  
  public int getBmlPort()
  {
    return bmlPort;
  }
  public int getFeedbackPort()
  {
    return feedbackPort;
  }
  public String getServerName()
  {
    return serverName;
  }
  
  public ServerInfo copy()
  {
    return new ServerInfo(serverName, bmlPort, feedbackPort);
  }
  
}