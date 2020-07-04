package javaclustering;

import entity.Cluster;
import entity.Trajectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class ClusteringJava {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusteringJava.class.getName());

    /*
     * Stores clusters in a list.
     *
     *
     */
    private List<Map<Integer, Cluster>> clusters = new ArrayList<>();


    /**
     * Single linkage
     * <p>
     * Implementation of Agglomerative Hierarchical Clustering of trajectories
     * \\Initialisation :
     * Initialize the clusters with one trajectory to each cluster
     * <p>
     * \\WHILE Process:
     * while number of clusters is greater than 1 do
     * - Calculate the similarity matrix D between pairs of clusters
     * - Find the smallest LCSS distance between clusters in D
     * - Merge the two clusters with minimum LCSS distance in a single cluster
     * - Remove the two clusters
     * end while
     *
     * @param trajectories A database of trajectories
     * @return Clusters of trajectories
     */
    public void cluster(List<Trajectory> trajectories) {
//    initialisation
        final int[] i = {0};
        trajectories.forEach(trajectory -> {
            clusters.add(new HashMap<Integer, Cluster>() {{
                put(i[0], new Cluster(trajectory));
            }});
            i[0]++;
        });
    }

    /**
     * Calculates LCSS distance for two input trajectories
     *
     * @param t1 first trajectory
     * @param t2 second trajectory
     * @return LCSS distance for t1 and t2
     */
    public Double calcLCSSDist(Trajectory t1, Trajectory t2) {
        int m = t1.length();
        int n = t2.length();

        double delta = getDelta(m, n);
        double epsilon = getEpsilon(m, n);

        double dist = 1 - calcLCSS(t1, t2, delta, epsilon) / min(m, n);
        return dist;
    }


    /**
     * Calculates LCSS for two input trajectories
     *
     * @param t1      first trajectory
     * @param t2      second trajectory
     * @param delta   δ parameter: how far we can look in time to match a given point from one T to a point in another T
     * @param epsilon ε parameter: the size of proximity in which to look for matches, 0 < ε < 1
     * @return LCSS for t1 and t2
     */
    private Double calcLCSS(Trajectory t1, Trajectory t2, Double delta, Double epsilon) {
        int m = t1.length();
        int n = t2.length();

        if (m == 0 || n == 0) {
            return 0.0;
        }

//      according to [8]: delta and epsilon as thresholds for X- and Y-axes respectively
//      Then the abscissa difference and ordinate difference are less than thresholds (they are relatively close to each other)
//      they are considered similar and LCSS distance is increased by 1
        else if (abs(t1.get(m - 1).getX() - t2.get(n - 1).getX()) < epsilon
                && abs(t1.get(m - 1).getY() - t2.get(n - 1).getY()) < epsilon
                && abs(m - n) <= delta) {
            return 1 + calcLCSS(rest(t1), rest(t2), delta, epsilon);
        } else {
            return max(calcLCSS(rest(t1), t2, delta, epsilon), calcLCSS(t1, rest(t2), delta, epsilon));
        }
    }

    /**
     * Calculates LCSS for two input trajectories
     *
     * @param t trajectory
     * @return trajectory without last trajectory point
     */
    private Trajectory rest(Trajectory t) {
        t.getTrajectoryPoints().remove(t.length() - 1);
        return t;
    }

    private Double getDelta(int m, int n) {
        return 0.5 * min(m, n);
    }

    private Double getEpsilon(int m, int n) {
        return 0.7;
    }


    /**
     * Calculates inter-clusters distance for two input clusters
     * using single-link method
     *
     * @param cluster1 first cluster
     * @param cluster2 second cluster
     * @return distance between clusters
     */
    private Double calcClustersDist(Cluster cluster1, Cluster cluster2) {
        return 0.0;
    }


}
