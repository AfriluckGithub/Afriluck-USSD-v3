mkdir -p ./postgres-init
echo "CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\";" > ./postgres-init/01-extensions.sql