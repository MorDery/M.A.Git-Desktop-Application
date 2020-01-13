package components.commitTree.commitGraphLayout;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import components.commitTree.commitNode.CommitNode;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CommitTreeLayout implements Layout {


    /*@Override*/
    public void execute(Graph graph) {
        int startX = 10;
        int startY = 50;
        final List<ICell> cells = graph.getModel().getAllCells();
        List<CommitNode> cellsDrawedInGraph = new ArrayList<>();
        Map<CommitNode,Integer> nodeToLocateXParameter = new HashMap<>();
        for (ICell cell : cells) {
            CommitNode currentCommitNode = (CommitNode) cell;
            List<CommitNode> cellChildrenThatDrawed = cellsDrawedInGraph.stream().filter(v->cell.getCellParents().contains(v)).collect(Collectors.toList());
            if(cellChildrenThatDrawed.isEmpty()){
                graph.getGraphic(currentCommitNode).relocate(startX, startY);
                nodeToLocateXParameter.put(currentCommitNode,startX);
                startX=startX+50;
            }
            else{
                CommitNode nodeTemp = null;
                List<CommitNode> nodeis = cellChildrenThatDrawed.stream().filter(v->v.getCellParents().size()==2).collect(Collectors.toList());
                ICell nodesOfCurrentCommit = currentCommitNode;
                if(!nodeis.isEmpty()){
                    nodeTemp = nodeis.get(0);
                    nodesOfCurrentCommit=nodeTemp.getCellParents().stream().filter(v->v!=currentCommitNode).collect(Collectors.toList()).get(0);
                }
                if(nodeTemp == null || !cellsDrawedInGraph.contains(nodesOfCurrentCommit)) {
                    int x = nodeToLocateXParameter.get(cellChildrenThatDrawed.get(0));
                    graph.getGraphic(currentCommitNode).relocate(x, startY);
                    nodeToLocateXParameter.put(currentCommitNode, x);
                }
                else{
                    graph.getGraphic(currentCommitNode).relocate(startX, startY);
                    nodeToLocateXParameter.put(currentCommitNode,startX);
                    startX = startX+50;
                }
            }
            cellsDrawedInGraph.add(currentCommitNode);
            startY=startY+50;
        }
    }
}


