# Master Thesis project
## Trajectory Anomalies Detection in Spatio-temporal Data

Project consists of module:
* **_java_**
    * input data (txt file) parsing and trajectories extraction
    * trajectories clustering and 
    * anomalies detection algorithms implementation

**Clustering**:
* Hierarchical Agglomerative approach

**Similarity measure**:
* Longest Common SubSequence (LCSS) distance

**Trajectories Preprocessing**

Due to high complexity and time consumption of LCSS, trajectories need to be preprocessed.

Polynomial Regression used to approximate trajectories with polynomials of 3rd and 4th order.

Implementation of Polynomial Regression by [Robert Sedgewick and Kevin Wayne](https://algs4.cs.princeton.edu/14analysis/PolynomialRegression.java). 

**Versions:**
* `Maven: 3.3.9`

* `Java: 1.8`

