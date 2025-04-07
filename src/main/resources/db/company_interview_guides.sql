-- SQL script to populate the company_interview_guides table
-- These INSERT statements will add general interview tips for companies

-- Disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data
TRUNCATE TABLE company_interview_guides;
ALTER TABLE company_interview_guides AUTO_INCREMENT = 1;

-- Enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Amazon Interview Guides
INSERT IGNORE INTO company_interview_guides (company_id, title, content, section) VALUES
(2, 'Amazon Leadership Principles', 
'<h2>Amazon Leadership Principles</h2>
<p>Amazon\'s 16 Leadership Principles are the foundation of their interview process. Expect behavioral questions that directly relate to these principles.</p>
<ul>
  <li><strong>Customer Obsession:</strong> Leaders start with the customer and work backwards.</li>
  <li><strong>Ownership:</strong> Leaders act on behalf of the entire company, beyond just their team.</li>
  <li><strong>Invent and Simplify:</strong> Leaders expect and require innovation from their teams.</li>
  <li><strong>Are Right, A Lot:</strong> Leaders have strong judgment and good instincts.</li>
  <li><strong>Learn and Be Curious:</strong> Leaders never stop learning and improving.</li>
  <li><strong>Hire and Develop the Best:</strong> Leaders raise the performance bar with every hire.</li>
  <li><strong>Insist on the Highest Standards:</strong> Leaders ensure problems are fixed and stay fixed.</li>
  <li><strong>Think Big:</strong> Leaders create and communicate bold directions that inspire results.</li>
  <li><strong>Bias for Action:</strong> Speed matters in business.</li>
  <li><strong>Frugality:</strong> Accomplish more with less.</li>
  <li><strong>Earn Trust:</strong> Leaders listen attentively and speak candidly.</li>
  <li><strong>Dive Deep:</strong> Leaders operate at all levels and stay connected to the details.</li>
  <li><strong>Have Backbone; Disagree and Commit:</strong> Leaders are obligated to challenge decisions they disagree with.</li>
  <li><strong>Deliver Results:</strong> Leaders focus on key inputs and deliver with the right quality.</li>
  <li><strong>Strive to be Earth\'s Best Employer:</strong> Leaders work every day to create a safer, more productive, higher performing, more diverse, and more just work environment.</li>
  <li><strong>Success and Scale Bring Broad Responsibility:</strong> Leaders create more than just value for customers—they create value for communities and the planet.</li>
</ul>
<p>Prepare examples from your experience that demonstrate these principles, using the STAR method (Situation, Task, Action, Result).</p>',
'OVERVIEW'),

(2, 'Amazon Technical Interview Format', 
'<h2>Amazon Technical Interview Format</h2>
<p>For software engineering roles, expect a multi-round interview process:</p>
<ol>
  <li><strong>Online Assessment:</strong> Typically includes 1-2 coding problems and sometimes work-style assessment questions.</li>
  <li><strong>Phone Screen:</strong> 45-60 minute technical interview with a single interviewer focusing on 1-2 coding problems.</li>
  <li><strong>On-site/Virtual Loop:</strong> 4-5 interviews including:
    <ul>
      <li>Coding interviews (2-3 rounds)</li>
      <li>System design interview (for SDE II and above)</li>
      <li>Behavioral interview focused on Leadership Principles</li>
    </ul>
  </li>
</ol>
<p><strong>Pro Tips:</strong></p>
<ul>
  <li>Use the STAR method for behavioral questions</li>
  <li>Verbalize your thought process during coding</li>
  <li>Write clean, scalable code with proper error handling</li>
  <li>Consider edge cases before being prompted</li>
  <li>Practice system design focusing on AWS services</li>
  <li>Show how your solutions align with Leadership Principles</li>
</ul>',
'PROCESS'),

-- Google Interview Guides
(1, 'Google Interview Process', 
'<h2>Google Interview Process</h2>
<p>Google\'s interview process is designed to evaluate your problem-solving skills, technical knowledge, and cultural fit.</p>
<ol>
  <li><strong>Phone/Video Screening:</strong> 1-2 technical interviews with coding via a shared document.</li>
  <li><strong>On-site/Virtual Loop:</strong> 4-5 interviews including:
    <ul>
      <li>Coding interviews (2-3 rounds)</li>
      <li>System design interview (for senior roles)</li>
      <li>Behavioral/"Googleyness" interview</li>
    </ul>
  </li>
  <li><strong>Hiring Committee Review:</strong> Your performance is evaluated by a committee of Googlers.</li>
  <li><strong>Offer Review:</strong> If approved by the committee, an offer is prepared.</li>
</ol>
<p><strong>What Google Looks For:</strong></p>
<ul>
  <li><strong>Coding:</strong> Clean, efficient code; strong algorithms and data structures knowledge</li>
  <li><strong>Problem-solving:</strong> Ability to approach problems methodically</li>
  <li><strong>System design:</strong> Designing scalable, reliable, maintainable systems</li>
  <li><strong>Googleyness:</strong> How well you\'ll thrive in Google\'s culture</li>
  <li><strong>Leadership:</strong> Taking initiative, mentoring others, influencing without authority</li>
</ul>',
'PROCESS'),

(1, 'Google Technical Preparation Tips', 
'<h2>Google Technical Preparation Tips</h2>
<p>Focus on these key areas to prepare for Google technical interviews:</p>
<h3>Data Structures</h3>
<ul>
  <li>Arrays and Strings</li>
  <li>Linked Lists</li>
  <li>Stacks and Queues</li>
  <li>Trees and Graphs</li>
  <li>Hash Tables</li>
  <li>Heaps</li>
  <li>Tries</li>
</ul>
<h3>Algorithms</h3>
<ul>
  <li>Sorting and Searching</li>
  <li>Recursion and Dynamic Programming</li>
  <li>BFS and DFS</li>
  <li>Greedy Algorithms</li>
  <li>Divide and Conquer</li>
</ul>
<h3>System Design</h3>
<ul>
  <li>Distributed Systems</li>
  <li>Scalability</li>
  <li>Load Balancing</li>
  <li>Caching</li>
  <li>Database Sharding</li>
  <li>API Design</li>
</ul>
<p><strong>Pro Tips:</strong></p>
<ul>
  <li>Think out loud and communicate your thought process</li>
  <li>Discuss trade-offs in your solutions</li>
  <li>Start with a brute force approach, then optimize</li>
  <li>Analyze time and space complexity</li>
  <li>Test your code with examples</li>
  <li>Practice on a whiteboard or Google Doc (not an IDE)</li>
</ul>',
'PREPARATION');

-- Add more guides for other companies 