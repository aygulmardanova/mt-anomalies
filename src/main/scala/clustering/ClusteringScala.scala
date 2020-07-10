package clustering

import scala.collection.JavaConverters._
import scala.math.{abs, max, min}
import entity.{Cluster, Trajectory}
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer

object ClusteringScala {

  private val LOGGER = LoggerFactory.getLogger(ClusteringScala.getClass)


  /*
  * Stores clusters in a list.
  *
  *
  */
  private val clusters: ListBuffer[Map[Int, Cluster]] = ListBuffer()

  /**
    * Single linkage
    *
    * Implementation of Agglomerative Hierarchical Clustering of trajectories
    * \\Initialisation :
    * Initialize the clusters with one trajectory to each cluster
    *
    * \\WHILE Process:
    * while number of clusters is greater than 1 do
    * - Calculate the similarity matrix D between pairs of clusters
    * - Find the smallest LCSS distance between clusters in D
    * - Merge the two clusters with minimum LCSS distance in a single cluster
    * - Remove the two clusters
    * end while
    *
    * @param trajectories A database of trajectories
    *
    * @return Clusters of trajectories
    */
  def cluster(trajectories: List[Trajectory]): Unit = {
//    initialisation
    val i = 0
    trajectories.foreach(trajectory => {
      val cluster = new Cluster(trajectory)
      clusters += null
    })
  }

  /**
    * Calculates LCSS distance for two input trajectories
    *
    * @param t1 first trajectory
    * @param t2 second trajectory
    *
    * @return LCSS distance for t1 and t2
    */
  def calcLCSSDist(t1: Trajectory, t2: Trajectory): Double = {
    val m = t1.length()
    val n = t2.length()

    val delta = getDelta(m, n)
    val epsilon = getEpsilon(m, n)

    val dist = 1 - calcLCSS(t1, t2, delta, epsilon) / min(m, n)
    dist
  }

  /**
    * Calculates LCSS for two input trajectories
    *
    * @param t1 first trajectory
    * @param t2 second trajectory
    * @param delta δ parameter: how far we can look in time to match a given point from one T to a point in another T
    * @param epsilon ε parameter: the size of proximity in which to look for matches, 0 < ε < 1
    *
    * @return LCSS for t1 and t2
    */
  private def calcLCSS(t1: Trajectory, t2: Trajectory, delta: Double, epsilon: Double): Double = {
    val m = t1.length()
    val n = t2.length()

    if (m == 0 || n == 0) {
      0
    }

//      according to [8]: delta and epsilon as thresholds for X- and Y-axes respectively
//      Then the abscissa difference and ordinate difference are less than thresholds (they are relatively close to each other)
//      they are considered similar and LCSS distance is increased by 1
    else if (abs(t1.get(m - 1).getX - t2.get(n - 1).getX) < epsilon
      && abs(t1.get(m - 1).getY - t2.get(n - 1).getY) < epsilon
      && abs(m - n) <= delta) {
      1 + calcLCSS(rest(t1), rest(t2), delta, epsilon)
    }

    else {
      max(calcLCSS(rest(t1), t2, delta, epsilon), calcLCSS(t1, rest(t2), delta, epsilon))
    }
  }

  /**
    * Calculates LCSS for two input trajectories
    *
    * @param t trajectory
    * @return trajectory without last trajectory point
    */
  def rest(t: Trajectory): Trajectory = {
    t.getTrajectoryPoints.remove(t.length() - 1)
    t
  }

  def getDelta(m: Int, n: Int): Double = {
    0.5 * min(m, n)
  }

  def getEpsilon(m: Int, n: Int): Double = {
    0.7
  }

  /**
    * Calculates inter-clusters distance for two input clusters
    * using single-link method
    *
    * @param cluster1 first cluster
    * @param cluster2 second cluster
    * @return distance between clusters
    */
  def calcClustersDist(cluster1: Cluster, cluster2: Cluster): Double = {
    0
  }
}
