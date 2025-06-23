#!/bin/bash

# Find the line number where "Get All Departments" ends
line_num=$(grep -n '"name": "Get All Departments"' Tymbl.postman_collection.json | head -1 | cut -d: -f1)

# Find the closing brace for the Get All Departments item
end_line=$(sed -n "${line_num},$p" Tymbl.postman_collection.json | grep -n "}" | head -1 | cut -d: -f1)
end_line=$((line_num + end_line - 1))

# Insert the new endpoints after the Get All Departments endpoint
sed -i '' "${end_line}a\\
        },\\
        {\\
          \"name\": \"Get All Companies for Dropdown\",\\
          \"request\": {\\
            \"method\": \"GET\",\\
            \"header\": [],\\
            \"url\": { \"raw\": \"{{baseUrl}}/api/v1/dropdowns/companies\", \"host\": [ \"{{baseUrl}}\" ], \"path\": [ \"api\", \"v1\", \"dropdowns\", \"companies\" ] },\\
            \"description\": \"Get all companies with id and name for dropdown selection.\"\\
          },\\
          \"response\": [\\
            { \\
              \"code\": 200, \\
              \"name\": \"List of companies retrieved successfully\", \\
              \"body\": \"[\\\\n  {\\\\n    \\\"id\\\": 1,\\\\n    \\\"name\\\": \\\"Google\\\"\\\\n  },\\\\n  {\\\\n    \\\"id\\\": 2,\\\\n    \\\"name\\\": \\\"Microsoft\\\"\\\\n  },\\\\n  {\\\\n    \\\"id\\\": 3,\\\\n    \\\"name\\\": \\\"Amazon\\\"\\\\n  }\\\\n]\" \\
            }\\
          ]\\
        },\\
        {\\
          \"name\": \"Get Companies as Map\",\\
          \"request\": {\\
            \"method\": \"GET\",\\
            \"header\": [],\\
            \"url\": { \"raw\": \"{{baseUrl}}/api/v1/dropdowns/companies-map\", \"host\": [ \"{{baseUrl}}\" ], \"path\": [ \"api\", \"v1\", \"dropdowns\", \"companies-map\" ] },\\
            \"description\": \"Get companies as a map of value/label pairs for dropdown components.\"\\
          },\\
          \"response\": [\\
            { \\
              \"code\": 200, \\
              \"name\": \"Companies map retrieved successfully\", \\
              \"body\": \"[\\\\n  {\\\\n    \\\"value\\\": \\\"1\\\",\\\\n    \\\"label\\\": \\\"Google\\\"\\\\n  },\\\\n  {\\\\n    \\\"value\\\": \\\"2\\\",\\\\n    \\\"label\\\": \\\"Microsoft\\\"\\\\n  },\\\\n  {\\\\n    \\\"value\\\": \\\"3\\\",\\\\n    \\\"label\\\": \\\"Amazon\\\"\\\\n  }\\\\n]\" \\
            }\\
          ]\\
" Tymbl.postman_collection.json

echo "Company dropdown endpoints added successfully!" 