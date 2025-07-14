-- SQL script to populate the interview_questions table
-- These INSERT statements will add questions related to interview topics

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE interview_questions;
ALTER TABLE interview_questions AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Add migration SQL to add topic_id and topic_name columns to interview_questions table. topic_id is BIGINT, topic_name is VARCHAR(255).
ALTER TABLE interview_questions ADD COLUMN topic_id BIGINT;
ALTER TABLE interview_questions ADD COLUMN topic_name VARCHAR(255);
ALTER TABLE interview_questions ADD COLUMN java_code LONGTEXT;
ALTER TABLE interview_questions ADD COLUMN python_code LONGTEXT;
ALTER TABLE interview_questions ADD COLUMN cpp_code LONGTEXT;
ALTER TABLE interview_questions ADD COLUMN coding BOOLEAN;

-- Questions for "Java Collections Framework" topic (topic_id = 1)
INSERT IGNORE INTO interview_questions (topic_id, question, answer, difficulty_level) VALUES
(1, 'What is the difference between ArrayList and LinkedList?', 
'<p>The key differences between <code>ArrayList</code> and <code>LinkedList</code> are:</p>
<table>
  <tr><th>Feature</th><th>ArrayList</th><th>LinkedList</th></tr>
  <tr>
    <td>Internal implementation</td>
    <td>Dynamic array</td>
    <td>Doubly linked list</td>
  </tr>
  <tr>
    <td>Random access</td>
    <td>O(1) - constant time access by index</td>
    <td>O(n) - linear time access by index</td>
  </tr>
  <tr>
    <td>Insertion/deletion in the middle</td>
    <td>O(n) - requires shifting elements</td>
    <td>O(1) - only needs to update references</td>
  </tr>
  <tr>
    <td>Memory overhead</td>
    <td>Lower - only needs to maintain array</td>
    <td>Higher - stores data and two references per element</td>
  </tr>
  <tr>
    <td>Iteration</td>
    <td>Faster due to better locality of reference</td>
    <td>Slower due to non-contiguous memory</td>
  </tr>
</table>
<p><strong>When to use ArrayList:</strong></p>
<ul>
  <li>When you need frequent random access to elements</li>
  <li>When you primarily add/remove elements at the end</li>
  <li>When memory usage is a concern</li>
</ul>
<p><strong>When to use LinkedList:</strong></p>
<ul>
  <li>When you frequently add/remove elements from the beginning or middle</li>
  <li>When implementing stacks or queues (it implements both List and Deque interfaces)</li>
  <li>When you need constant-time insertion and deletion</li>
</ul>
<p>At Amazon, this question often leads to discussions about how you would select the appropriate data structure for specific use cases in their systems.</p>', 
'MEDIUM'),

(1, 'Explain the difference between HashMap and ConcurrentHashMap. When would you use one over the other?', 
'<p><strong>HashMap vs ConcurrentHashMap</strong></p>
<p>Key differences:</p>
<ul>
  <li><strong>Thread Safety</strong>: HashMap is not thread-safe, while ConcurrentHashMap is designed for concurrent access.</li>
  <li><strong>Synchronization</strong>: HashMap has no synchronization. ConcurrentHashMap uses a more efficient segment-level locking (pre-Java 8) or fine-grained locking at the node level (Java 8+).</li>
  <li><strong>Null values</strong>: HashMap allows one null key and multiple null values. ConcurrentHashMap does not allow null keys or values.</li>
  <li><strong>Performance</strong>: HashMap is faster in single-threaded environments. ConcurrentHashMap performs better in multi-threaded environments.</li>
  <li><strong>Iterator behavior</strong>: HashMap iterator is fail-fast (throws ConcurrentModificationException if modified during iteration). ConcurrentHashMap iterator is weakly consistent (reflects the state at construction time but may accommodate modifications during iteration).</li>
</ul>
<p><strong>When to use HashMap:</strong></p>
<ul>
  <li>In single-threaded applications</li>
  <li>When null keys or values are needed</li>
  <li>When maximum performance in a non-concurrent context is required</li>
</ul>
<p><strong>When to use ConcurrentHashMap:</strong></p>
<ul>
  <li>In multi-threaded applications where multiple threads read and write concurrently</li>
  <li>When you need to avoid the performance bottleneck of synchronized collections like Hashtable</li>
  <li>In high-concurrency scenarios where map data is shared across threads</li>
</ul>
<p>At Amazon, where distributed systems are common, understanding the thread-safety implications of collection choices is crucial. You might be asked to explain how you would handle a scenario where a map is accessed by multiple services or threads.</p>',
'HARD'),

-- Questions for "Java Concurrency" topic (topic_id = 2)
(2, 'What is the difference between "synchronized" keyword and Lock interface in Java?',
'<p>The <code>synchronized</code> keyword and the <code>Lock</code> interface are both mechanisms for controlling concurrent access to shared resources in Java, but they differ in several important ways:</p>

<table>
  <tr><th>Feature</th><th>synchronized</th><th>Lock Interface</th></tr>
  <tr>
    <td>Flexibility</td>
    <td>Less flexible, implicit locking mechanism</td>
    <td>More flexible, explicit control over locking</td>
  </tr>
  <tr>
    <td>Lock acquisition</td>
    <td>All-or-nothing approach, cannot be interrupted</td>
    <td>Provides tryLock() with timeout support, can be interrupted</td>
  </tr>
  <tr>
    <td>Fairness</td>
    <td>No fairness guarantee (can lead to thread starvation)</td>
    <td>Can create fair locks where the longest waiting thread gets access first</td>
  </tr>
  <tr>
    <td>Multiple locks</td>
    <td>Cannot hold a lock on one object while waiting for another</td>
    <td>Can acquire multiple locks without risk of deadlock (using tryLock)</td>
  </tr>
  <tr>
    <td>Performance</td>
    <td>In simple cases, may be more efficient due to JVM optimizations</td>
    <td>More overhead but better scalability in complex scenarios</td>
  </tr>
  <tr>
    <td>Syntax</td>
    <td>Simpler syntax with synchronized blocks or methods</td>
    <td>More verbose with explicit lock() and unlock() calls</td>
  </tr>
</table>

<p><strong>Example of synchronized:</strong></p>
<pre><code>synchronized(lockObject) {
    // Critical section
}
</code></pre>

<p><strong>Example of Lock interface:</strong></p>
<pre><code>Lock lock = new ReentrantLock();
try {
    lock.lock();
    // Critical section
} finally {
    lock.unlock();  // Must be in finally block
}
</code></pre>

<p>At Amazon, this question often leads to discussions about handling concurrency in distributed systems and how you would design for thread safety while maintaining performance.</p>',
'HARD'),

-- Questions for "AWS S3 and DynamoDB" topic (topic_id = 4)
(4, 'Compare and contrast Amazon S3 and DynamoDB. When would you choose one over the other?',
'<p><strong>S3 vs DynamoDB: Comparison</strong></p>

<table>
  <tr><th>Feature</th><th>Amazon S3</th><th>Amazon DynamoDB</th></tr>
  <tr>
    <td>Type</td>
    <td>Object storage service</td>
    <td>NoSQL database service</td>
  </tr>
  <tr>
    <td>Data model</td>
    <td>Object-based with key-value retrieval</td>
    <td>Key-value and document data models</td>
  </tr>
  <tr>
    <td>Primary use cases</td>
    <td>Static file storage, backups, data lakes, web hosting</td>
    <td>Web applications, mobile backends, microservices, real-time applications</td>
  </tr>
  <tr>
    <td>Query capabilities</td>
    <td>Limited to GET/PUT/DELETE on objects</td>
    <td>Rich query options with primary and secondary indexes</td>
  </tr>
  <tr>
    <td>Data size limits</td>
    <td>Individual objects up to 5TB, unlimited storage</td>
    <td>Individual items up to 400KB</td>
  </tr>
  <tr>
    <td>Latency</td>
    <td>Higher latency (tens to hundreds of ms)</td>
    <td>Single-digit millisecond latency</td>
  </tr>
  <tr>
    <td>Consistency</td>
    <td>Read-after-write consistency for PUT operations, eventual consistency for DELETE/UPDATE operations</td>
    <td>Configurable: eventually consistent or strongly consistent reads</td>
  </tr>
  <tr>
    <td>Transactions</td>
    <td>No transaction support</td>
    <td>Supports ACID transactions across multiple items and tables</td>
  </tr>
</table>

<p><strong>When to use S3:</strong></p>
<ul>
  <li>Storing large files and static content</li>
  <li>Content distribution and CDN integration</li>
  <li>Building data lakes for analytics</li>
  <li>Backup and archival storage</li>
  <li>When you need to store objects larger than 400KB</li>
</ul>

<p><strong>When to use DynamoDB:</strong></p>
<ul>
  <li>Applications requiring low-latency access regardless of scale</li>
  <li>Serverless applications with variable workloads</li>
  <li>When you need rich query capabilities on structured data</li>
  <li>Real-time applications requiring consistent performance</li>
  <li>When you need transactional support across data items</li>
</ul>

<p>At Amazon, understanding these services is crucial as most applications leverage them. You might be asked to design a system that uses both - for example, storing large media files in S3 while keeping metadata and relationships in DynamoDB.</p>',
'MEDIUM'),

-- Questions for "Sorting Algorithms" topic (topic_id = 6)
(6, 'Implement QuickSort algorithm and analyze its time complexity in best, average, and worst cases.',
'<p><strong>QuickSort Implementation in Java:</strong></p>

<pre><code>
public void quickSort(int[] arr, int low, int high) {
    if (low < high) {
        // pi is partitioning index
        int pi = partition(arr, low, high);

        // Recursively sort elements before and after partition
        quickSort(arr, low, pi - 1);
        quickSort(arr, pi + 1, high);
    }
}

private int partition(int[] arr, int low, int high) {
    // Taking the rightmost element as pivot
    int pivot = arr[high];
    int i = low - 1; // Index of smaller element

    for (int j = low; j < high; j++) {
        // If current element is smaller than the pivot
        if (arr[j] < pivot) {
            i++;

            // Swap arr[i] and arr[j]
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    // Swap arr[i+1] and arr[high] (or pivot)
    int temp = arr[i + 1];
    arr[i + 1] = arr[high];
    arr[high] = temp;

    return i + 1;
}
</code></pre>

<p><strong>Time Complexity Analysis:</strong></p>

<p><em>Best Case: O(n log n)</em></p>
<p>This occurs when the pivot divides the array into two roughly equal halves each time. The recurrence relation is T(n) = 2T(n/2) + O(n), which resolves to O(n log n).</p>

<p><em>Average Case: O(n log n)</em></p>
<p>On average, QuickSort performs similarly to the best case, with a time complexity of O(n log n).</p>

<p><em>Worst Case: O(n²)</em></p>
<p>This occurs when the pivot selection results in the most unbalanced partition possible - for example, when the array is already sorted and we always choose the rightmost element as pivot. The recurrence relation becomes T(n) = T(n-1) + O(n), which resolves to O(n²).</p>

<p><strong>Space Complexity: O(log n)</strong> on average due to the recursion stack, but <strong>O(n)</strong> in the worst case.</p>

<p><strong>Optimization Strategies:</strong></p>
<ul>
  <li><strong>Pivot Selection</strong>: Use median-of-three method rather than always selecting the rightmost element</li>
  <li><strong>Randomized QuickSort</strong>: Randomly select the pivot to avoid worst-case scenarios</li>
  <li><strong>Hybrid Approach</strong>: Switch to Insertion Sort for small subarrays (typically less than 10 elements)</li>
  <li><strong>Tail Recursion Elimination</strong>: Optimize recursive calls to reduce stack space</li>
</ul>

<p>Google interviewers might ask you to optimize this code further or discuss specific use cases where QuickSort is preferred over other sorting algorithms like MergeSort.</p>',
'HARD'),

-- Questions for "Graph Algorithms" topic (topic_id = 7)
(7, 'Implement Dijkstra\'s algorithm to find the shortest path in a weighted graph.',
'<p><strong>Dijkstra\'s Algorithm Implementation in Java:</strong></p>

<pre><code>
import java.util.*;

class Graph {
    private int V; // Number of vertices
    private List&lt;List&lt;Node&gt;&gt; adj;

    class Node {
        int vertex, weight;

        Node(int v, int w) {
            vertex = v;
            weight = w;
        }
    }

    Graph(int vertices) {
        V = vertices;
        adj = new ArrayList&lt;&gt;();
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList&lt;&gt;());
        }
    }

    void addEdge(int u, int v, int weight) {
        adj.get(u).add(new Node(v, weight));
        adj.get(v).add(new Node(u, weight)); // For undirected graph
    }

    int[] dijkstra(int src) {
        // Array to store shortest distance from src to i
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);
        dist[src] = 0;

        // Priority queue to get minimum distance vertex
        PriorityQueue&lt;Node&gt; pq = new PriorityQueue&lt;&gt;(
            Comparator.comparingInt(node -> node.weight));

        // Add source to pq
        pq.add(new Node(src, 0));

        // Set to keep track of vertices already processed
        Set&lt;Integer&gt; settled = new HashSet&lt;&gt;();

        while (!pq.isEmpty()) {
            // Remove the minimum distance vertex
            int u = pq.poll().vertex;

            // If already processed, skip
            if (settled.contains(u)) {
                continue;
            }

            // Mark as processed
            settled.add(u);

            // Process neighbors
            for (Node neighbor : adj.get(u)) {
                int v = neighbor.vertex;
                int weight = neighbor.weight;

                // If v is not in settled and distance needs to be updated
                if (!settled.contains(v) && dist[u] != Integer.MAX_VALUE &&
                    dist[u] + weight < dist[v]) {
                    // Update distance
                    dist[v] = dist[u] + weight;
                    // Add to priority queue
                    pq.add(new Node(v, dist[v]));
                }
            }
        }

        return dist;
    }
}

// Example usage:
// Graph g = new Graph(9);
// g.addEdge(0, 1, 4); g.addEdge(0, 7, 8); // Add edges
// int[] distances = g.dijkstra(0); // Find shortest paths from vertex 0
</code></pre>

<p><strong>Time Complexity Analysis:</strong></p>
<p>Using a binary heap (PriorityQueue): O((V+E)log V), where V is the number of vertices and E is the number of edges.</p>

<p><strong>Space Complexity: O(V)</strong> for storing the distances and the priority queue.</p>

<p><strong>Key Points:</strong></p>
<ul>
  <li>Dijkstra\'s algorithm finds the shortest path from a source vertex to all other vertices in a weighted graph.</li>
  <li>It doesn\'t work with negative weights (Bellman-Ford should be used instead).</li>
  <li>The original implementation has O(V²) time complexity, but using a priority queue improves it to O((V+E)log V).</li>
  <li>Using a Fibonacci heap can further improve theoretical time complexity to O(E + V log V), but it\'s more complex to implement.</li>
</ul>

<p><strong>Optimization Tips:</strong></p>
<ul>
  <li>Stop the algorithm once the destination vertex is reached (for single-source, single-destination)</li>
  <li>Use bidirectional search for faster results in some scenarios</li>
  <li>Consider using A* algorithm when heuristic information is available</li>
</ul>

<p>Google interviewers might ask you to modify this algorithm for specific use cases, such as finding k shortest paths, or adapting it for different graph representations.</p>',
'HARD');

-- Add more questions for other topics