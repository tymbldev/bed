-- SQL script to populate the interview_topics table
-- These INSERT statements will add topics for company-designation-skill combinations

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE interview_topics;
ALTER TABLE interview_topics AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Topics for Amazon Software Engineer - Java (company_designation_skill_id = 1)
INSERT IGNORE INTO interview_topics (company_designation_skill_id, title, description, content, difficulty_level) VALUES
(1, 'Java Collections Framework', 'Overview of Java Collections and common interview questions related to it', 
'<h2>Java Collections Framework Overview</h2>
<p>The Java Collections Framework is a unified architecture for representing and manipulating collections, enabling them to be manipulated independently of the details of their representation.</p>
<p>Key interfaces in the Collections framework include:</p>
<ul>
  <li>Collection - the root of the collection hierarchy</li>
  <li>List - an ordered collection</li>
  <li>Set - a collection that does not allow duplicate elements</li>
  <li>Map - an object that maps keys to values</li>
</ul>
<p>Common implementations include ArrayList, LinkedList, HashSet, TreeSet, HashMap, and TreeMap.</p>
<p>For Amazon interviews, focus on understanding the time and space complexity of different operations on these data structures, as well as when to use each one.</p>', 
'MEDIUM'),

(1, 'Java Concurrency', 'Thread management and synchronization in Java', 
'<h2>Java Concurrency</h2>
<p>Java provides built-in support for multithreaded programming. Topics that often come up in Amazon interviews include:</p>
<ul>
  <li>Thread creation and lifecycle</li>
  <li>Synchronization and locks</li>
  <li>wait(), notify(), and notifyAll()</li>
  <li>Concurrent collections (ConcurrentHashMap, BlockingQueue, etc.)</li>
  <li>Executor framework</li>
  <li>CompletableFuture</li>
</ul>
<p>Be prepared to discuss race conditions, deadlocks, and thread safety. Amazon often asks candidates to identify concurrency issues in code.</p>', 
'HARD'),

(1, 'Java Memory Management', 'Understanding garbage collection and memory allocation in Java', 
'<h2>Java Memory Management</h2>
<p>Java manages memory automatically through garbage collection. Key concepts to understand include:</p>
<ul>
  <li>Stack vs Heap memory</li>
  <li>Garbage Collection process</li>
  <li>Memory leaks in Java</li>
  <li>References (Strong, Weak, Soft, Phantom)</li>
  <li>Finalization</li>
</ul>
<p>Amazon interviewers may ask about how to optimize memory usage or identify memory leaks in Java applications.</p>', 
'MEDIUM'),

-- Topics for Amazon Software Engineer - AWS (company_designation_skill_id = 5)
(5, 'AWS S3 and DynamoDB', 'Understanding key AWS storage services', 
'<h2>Amazon S3 and DynamoDB</h2>
<p>Amazon S3 (Simple Storage Service) and DynamoDB are two of the most widely used AWS services.</p>
<h3>S3</h3>
<ul>
  <li>Object storage service</li>
  <li>Highly durable and available</li>
  <li>Used for storing static files, backups, and data lakes</li>
  <li>Storage classes (Standard, Infrequent Access, Glacier)</li>
  <li>Versioning and lifecycle policies</li>
</ul>
<h3>DynamoDB</h3>
<ul>
  <li>NoSQL database service</li>
  <li>Single-digit millisecond performance</li>
  <li>Fully managed, serverless</li>
  <li>Partition key and sort key concepts</li>
  <li>Read/write capacity modes</li>
</ul>
<p>Amazon often asks about designing solutions using these services, particularly around performance optimization and cost management.</p>', 
'MEDIUM'),

(5, 'AWS Lambda and API Gateway', 'Serverless computing on AWS', 
'<h2>AWS Lambda and API Gateway</h2>
<p>Serverless architecture is a common topic in Amazon interviews.</p>
<h3>Lambda</h3>
<ul>
  <li>Event-driven, serverless computing</li>
  <li>Pay-per-use model</li>
  <li>Supported runtimes (Node.js, Python, Java, etc.)</li>
  <li>Cold starts and optimization</li>
  <li>Concurrency limits</li>
</ul>
<h3>API Gateway</h3>
<ul>
  <li>Fully managed service for APIs</li>
  <li>Request/response transformations</li>
  <li>API keys and throttling</li>
  <li>Integration with Lambda</li>
  <li>Stages and deployments</li>
</ul>
<p>Expect questions on designing serverless architectures for different use cases and handling limitations of serverless computing.</p>', 
'HARD'),

-- Topics for Google Software Engineer - Algorithms (company_designation_skill_id = 17)
(17, 'Sorting Algorithms', 'Common sorting algorithms and their complexities', 
'<h2>Sorting Algorithms</h2>
<p>Google interviews frequently cover sorting algorithms and their characteristics.</p>
<table>
  <tr><th>Algorithm</th><th>Average Time</th><th>Best Time</th><th>Worst Time</th><th>Space</th><th>Stable</th></tr>
  <tr><td>Bubble Sort</td><td>O(n²)</td><td>O(n)</td><td>O(n²)</td><td>O(1)</td><td>Yes</td></tr>
  <tr><td>Selection Sort</td><td>O(n²)</td><td>O(n²)</td><td>O(n²)</td><td>O(1)</td><td>No</td></tr>
  <tr><td>Insertion Sort</td><td>O(n²)</td><td>O(n)</td><td>O(n²)</td><td>O(1)</td><td>Yes</td></tr>
  <tr><td>Merge Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n)</td><td>Yes</td></tr>
  <tr><td>Quick Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n²)</td><td>O(log n)</td><td>No</td></tr>
  <tr><td>Heap Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(1)</td><td>No</td></tr>
  <tr><td>Radix Sort</td><td>O(nk)</td><td>O(nk)</td><td>O(nk)</td><td>O(n+k)</td><td>Yes</td></tr>
</table>
<p>Be prepared to implement these algorithms, analyze their performance characteristics, and explain when to use one over another.</p>', 
'MEDIUM'),

(17, 'Graph Algorithms', 'Common graph traversal and shortest path algorithms', 
'<h2>Graph Algorithms</h2>
<p>Graph problems are extremely common in Google interviews. Key algorithms to know include:</p>
<h3>Graph Traversal</h3>
<ul>
  <li>Breadth-First Search (BFS) - O(V+E)</li>
  <li>Depth-First Search (DFS) - O(V+E)</li>
</ul>
<h3>Shortest Path</h3>
<ul>
  <li>Dijkstra\'s Algorithm - O((V+E)log V) with binary heap</li>
  <li>Bellman-Ford - O(VE)</li>
  <li>Floyd-Warshall - O(V³)</li>
</ul>
<h3>Minimum Spanning Tree</h3>
<ul>
  <li>Kruskal\'s Algorithm - O(E log E)</li>
  <li>Prim\'s Algorithm - O(E log V)</li>
</ul>
<p>Be prepared to apply these algorithms to solve problems related to networks, routing, social connections, etc.</p>', 
'HARD');

-- Add more topics for other company-designation-skill combinations 