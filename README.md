use this if the code so mess up and you want to go back from the start

git fetch origin  # Fetch the latest commits from GitHub
git reset --hard $(git rev-list --max-parents=0 HEAD)  # Reset to the first commit
git pull origin main --force  # Get a fresh copy from GitHub
