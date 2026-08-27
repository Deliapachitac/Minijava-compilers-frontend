#!/usr/bin/env python3
import os
import subprocess
import sys
 
#Collect all test files in the tests directory
tests_dir = "tests"
test_files = []
for f in  os.listdir(tests_dir):
    if f.endswith(".java"):
        test_files.append(os.path.join(tests_dir, f))
test_files.sort()
 
#Run each test file using the main class
for filepath in test_files:
    print(f"\n{'='*40}")
    print(f"Running: {filepath}")
    print('='*40)
    subprocess.run(["java", "-cp", ".", "main", filepath])
