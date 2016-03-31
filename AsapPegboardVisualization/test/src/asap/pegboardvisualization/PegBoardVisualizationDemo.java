/*******************************************************************************
 *******************************************************************************/
package asap.pegboardvisualization;

import javax.swing.JFrame;

import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizertestutil.util.TimePegUtil;

/**
 * Test program to play around and try out the visualization
 * @author hvanwelbergen
 *
 */
public class PegBoardVisualizationDemo
{
    public static void main(String args[])
    {
        PegBoard pb = new PegBoard();
        
        pb.addBMLBlockPeg(new BMLBlockPeg("bml1",10));
        pb.addTimePeg("bml1","beh1", "start", TimePegUtil.createTimePeg(11));        
        pb.addTimePeg("bml1","beh1", "end", TimePegUtil.createTimePeg(15));
        pb.addTimePeg("bml1","beh2", "start", TimePegUtil.createTimePeg(12));
        pb.addTimePeg("bml1","beh2", "strokeStart", TimePegUtil.createTimePeg(13));
        pb.addTimePeg("bml1","beh2", "strokeEnd", TimePegUtil.createTimePeg(14));
        pb.addTimePeg("bml1","beh2", "end", TimePegUtil.createTimePeg(20));
        
        
        pb.addBMLBlockPeg(new BMLBlockPeg("bml2",30));
        pb.addTimePeg("bml2","beh1", "start", TimePegUtil.createTimePeg(31));        
        pb.addTimePeg("bml2","beh1", "end", TimePegUtil.createTimePeg(35));
        pb.addTimePeg("bml2","beh2", "start", TimePegUtil.createTimePeg(32));
        pb.addTimePeg("bml2","beh2", "strokeStart", TimePegUtil.createTimePeg(33));
        pb.addTimePeg("bml2","beh2", "strokeEnd", TimePegUtil.createTimePeg(34));
        pb.addTimePeg("bml2","beh2", "end", TimePegUtil.createTimePeg(40));
        
        JFrame jf = new JFrame();
        jf.add(new PegBoardVisualizer(pb));
        jf.setSize(1024,768);
        jf.setVisible(true);
        
        pb.addBMLBlockPeg(new BMLBlockPeg("bml3",30));
        pb.addTimePeg("bml3","beh1", "start", TimePegUtil.createTimePeg(31));        
        pb.addTimePeg("bml3","beh1", "end", TimePegUtil.createTimePeg(100));        
    }
}
