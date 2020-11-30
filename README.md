# Master Thesis project
## Trajectory Anomalies Detection in Spatio-temporal Data

Project consists of module written in:
* **Java** with following functionality:
    * input data (txt file) parsing and trajectories extraction
    * trajectories clustering and 
    * anomalies detection algorithms implementation
        * clusters classification
        * clusters modeling 
        * input trajectory classification

**Clustering**:
* Hierarchical Agglomerative approach
    * static ε
    * adaptive ε
* DBSCAN
    * static ε

**Similarity measure**:
* Longest Common SubSequence (LCSS) distance

**Trajectories Preprocessing**

Due to high complexity and time consumption of LCSS, trajectories need to be preprocessed.

Approximation methods:

* Polynomial Regression with polynomials of 3rd and 4th order,

* RDP algorithm.

* Douglas-Peucker N (N = 8 points).

Implementation of Polynomial Regression by [Robert Sedgewick and Kevin Wayne](https://algs4.cs.princeton.edu/14analysis/PolynomialRegression.java). 
Implementation of traditional RDP algorithm by [Lukasz Wiktor](https://github.com/LukaszWiktor/series-reducer).

**Versions:**
* `Maven: 3.3.9`

* `Java: 11`

* `Apache Commons Math3: 3.4.1`

