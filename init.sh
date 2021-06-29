echo "Leser secrets fra disk til environment"

export TEST_EXPORT=hellooo

echo $TEST_EXPORT

if test -f "/secrets/serviceuser/username"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/serviceuser/username)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
fi

if test -f "/secrets/oracle-q1/config/jdbc_url"; then
  export DATASOURCE_URL=$(cat /secrets/oracle-q1/config/jdbc_url)
  echo "Eksporterer variabel DATASOURCE_URL"
fi

if test -f "/secrets/oracle-q1/user/username"; then
  export DATASOURCE_USERNAME=$(cat /secrets/oracle-q1/user/username)
  echo "Eksporterer variabel DATASOURCE_USERNAME"
fi

if test -f "/secrets/oracle-q1/user/password"; then
  export DATASOURCE_PASSWORD=$(cat /secrets/oracle-q1/user/password)
  echo "Eksporterer variabel DATASOURCE_PASSWORD"
fi
