/*******************************************************************************
 *******************************************************************************/
package asap.pegboardvisualization;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import lombok.Data;
import lombok.Setter;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;

/**
 * Visualization panel for the pegboard
 * @author hvanwelbergen
 * 
 */
public class PegBoardVisualizer extends JPanel
{
    private static final long serialVersionUID = 1L;
    private PegBoard pegBoard;
    private static final int BEHAVIOR_HEIGHT = 10;
    private static final int ROOM_BETWEEN_BMLBLOCKS = 10;
    private static final int ROOM_BETWEEN_BEHAVIORS = 2;
    
    @Setter
    private double scale = 10;// 10 pixels/sec

    @Data
    private static final class TooltipInRectangle
    {
        private final Shape shape;
        private final String text;
    }

    private List<TooltipInRectangle> toolTips = new ArrayList<TooltipInRectangle>();

    public void setPegBoard(PegBoard pb)
    {
        this.pegBoard = pb;
        repaint();
    }
    
    public PegBoardVisualizer(PegBoard pb)
    {
        this.pegBoard = pb;
        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);

        addMouseMotionListener(new MouseMotionListener()
        {

            @Override
            public void mouseMoved(MouseEvent e)
            {
                ToolTipManager.sharedInstance().setEnabled(false);
                for (TooltipInRectangle tip : toolTips)
                {
                    if (tip.getShape().contains(e.getPoint()))
                    {
                        ToolTipManager.sharedInstance().setEnabled(true);
                        setToolTipText(tip.getText());
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e)
            {

            }
        });
    }

    private int drawBMLBlock(Graphics2D g2, String bmlId, BMLBlockPeg blockPeg, int y, double startTime)
    {
        g2.drawString(bmlId, (int)((blockPeg.getValue()-startTime)*scale), y+20);
        y += 25;
        
        for (String behId : pegBoard.getBehaviours(bmlId))
        {
            drawBehavior(g2, bmlId, behId, y, startTime);
            y += BEHAVIOR_HEIGHT + ROOM_BETWEEN_BEHAVIORS;
        }
        return y;
    }

    @Data
    private static final class TimeStamp
    {
        final String sync;
        final double time;
    }

    private void drawBehaviorPhase(Graphics2D g2, String bmlId, String behaviourId, TimeStamp start, TimeStamp end, int y,
            double startOffset)
    {
        double startTime = start.time - startOffset;
        double endTime = end.time - startOffset;
        Rectangle behaviorPhaseVis = new Rectangle((int) (startTime * scale), y, (int) ((endTime - startTime) * scale), BEHAVIOR_HEIGHT);
        toolTips.add(new TooltipInRectangle(behaviorPhaseVis, behaviourId + ":" + start.sync + "(" + start.time + ")" + "-" + end.sync
                + "(" + end.time + ")"));
        g2.draw(behaviorPhaseVis);
    }

    private void drawBehavior(Graphics2D g2, String bmlId, String behaviourId, int y, double startOffset)
    {
        List<TimeStamp> times = new ArrayList<TimeStamp>();
        for (String sync : pegBoard.getTimedSyncs(bmlId, behaviourId))
        {
            times.add(new TimeStamp(sync, pegBoard.getPegTime(bmlId, behaviourId, sync)));
        }
        Collections.sort(times, new Comparator<TimeStamp>()
        {
            @Override
            public int compare(TimeStamp t1, TimeStamp t2)
            {
                return Double.compare(t1.time, t2.time);
            }
        });

        TimeStamp tPrev = null;
        for (TimeStamp tCurr : times)
        {
            if (tPrev != null)
            {
                drawBehaviorPhase(g2, bmlId, behaviourId, tPrev, tCurr, y, startOffset);
            }
            tPrev = tCurr;
        }
    }

    private double getStartTime()
    {
        double startTime = Double.MAX_VALUE;
        for (BMLBlockPeg peg : pegBoard.getBMLBlockPegs().values())
        {
            if (peg != BMLBlockPeg.GLOBALPEG)
            {
                if (peg.getValue() < startTime)
                {
                    startTime = peg.getValue();
                }
            }
        }
        return startTime;
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        toolTips.clear();
        int y = 0;
        double startTime = getStartTime();
        for (Map.Entry<String, BMLBlockPeg> entry : pegBoard.getBMLBlockPegs().entrySet())
        {
            if (entry.getValue() != BMLBlockPeg.GLOBALPEG)
            {
                y = drawBMLBlock(g2, entry.getKey(), entry.getValue(), y, startTime);
                y += ROOM_BETWEEN_BMLBLOCKS;
            }
        }
    }
}
