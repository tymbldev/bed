#!/bin/bash

# Test script for the notification system
echo "Testing notification system endpoints..."

# Base URL - using correct port and context path
BASE_URL="http://localhost:8085/tymbl-service/api/v1/notifications"

echo "1. Testing get recent notifications for user 1..."
curl -X GET "$BASE_URL/recent/1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n2. Testing get unread notifications for user 1..."
curl -X GET "$BASE_URL/unread/1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n3. Testing get unread count for user 1..."
curl -X GET "$BASE_URL/unread-count/1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n4. Testing get all notifications for user 1..."
curl -X GET "$BASE_URL/all/1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n5. Testing mark notification as read..."
curl -X PUT "$BASE_URL/mark-read/1?userId=1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n6. Testing mark all notifications as read for user 1..."
curl -X PUT "$BASE_URL/mark-all-read/1" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\n7. Testing get notifications by type (REFERRAL_APPLICATION)..."
curl -X GET "$BASE_URL/type/1?type=REFERRAL_APPLICATION" \
  -H "Content-Type: application/json" \
  | jq '.'

echo -e "\n\nTest completed!" 