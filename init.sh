echo "Leser secrets fra disk til environment"


if test -f "/secrets/serviceuser/username"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/serviceuser/username)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
fi

if test -f "/secrets/sendsoknad-q1/jdbc_url"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/sendsoknad-q1/jdbc_url)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
fi

if test -f "/secrets/sendsoknad-q1/username"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/sendsoknad-q1/username)
  echo "Eksporterer variabel ORACLE_USERNAME"
fi

if test -f "/secrets/sendsoknad-q1/password"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/sendsoknad-q1/password)
  echo "Eksporterer variabel ORACLE_PASSWORD"
fi
