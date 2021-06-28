echo "Leser secrets fra disk til environment"

export TEST_EXPORT=hellooo

if test -f "/secrets/serviceuser/username"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/serviceuser/username)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
fi

if test -f "/secrets/sendsoknad-q1/jdbc_url"; then
  export DATASOURCE_URL=$(cat /secrets/sendsoknad-q1/jdbc_url)
  echo "Eksporterer variabel DATASOURCE_URL"
fi

if test -f "/secrets/sendsoknad-q1/username"; then
  export DATASOURCE_USERNAME=$(cat /secrets/sendsoknad-q1/username)
  echo "Eksporterer variabel DATASOURCE_USERNAME"
fi

if test -f "/secrets/sendsoknad-q1/password"; then
  export DATASOURCE_PASSWORD=$(cat /secrets/sendsoknad-q1/password)
  echo "Eksporterer variabel DATASOURCE_PASSWORD"
fi
