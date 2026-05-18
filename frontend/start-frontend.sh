#!/bin/bash
cd /home/brielmarca/Documents/forge-instinct-site-main
npm run dev -- --host > /tmp/frontend.log 2>&1 &
echo $! > /tmp/frontend.pid
echo "Frontend started with PID: $(cat /tmp/frontend.pid)"
