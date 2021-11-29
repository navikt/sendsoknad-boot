echo "Leser secrets fra disk til environment"

if test -f "/secrets/innsending-data/username"; then
  export INNSENDING_USERNAME=$(cat /secrets/innsending-data/username)
  echo "Eksporterer variabel INNSENDING_USERNAME"
fi

if test -f "/secrets/innsending-data/password"; then
  export INNSENDING_PASSWORD=$(cat /secrets/innsending-data/password)
  echo "Eksporterer variabel INNSENDING_PASSWORD"
fi

if test -f "/secrets/serviceuser/username"; then
  export SERVICEUSER_USERNAME=$(cat /secrets/serviceuser/username)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
fi

if test -f "/secrets/serviceuser/password"; then
  export SERVICEUSER_PASSWORD=$(cat /secrets/serviceuser/password)
  echo "Eksporterer variabel SERVICEUSER_PASSWORD"
fi

if test -f "/secrets/personinformasjon/username"; then
  export PERSONINFORMASJON_USERNAME=$(cat /secrets/personinformasjon/username)
  echo "Eksporterer variabel PERSONINFORMASJON_USERNAME"
fi

if test -f "/secrets/personinformasjon/password"; then
  export PERSONINFORMASJON_PASSWORD=$(cat /secrets/personinformasjon/password)
  echo "Eksporterer variabel PERSONINFORMASJON_PASSWORD"
fi

if test -f "/secrets/oracle/config/jdbc_url"; then
  export DATASOURCE_URL=$(cat /secrets/oracle/config/jdbc_url)
  echo "Eksporterer variabel DATASOURCE_URL"
fi

if test -f "/secrets/oracle/user/username"; then
  export DATASOURCE_USERNAME=$(cat /secrets/oracle/user/username)
  echo "Eksporterer variabel DATASOURCE_USERNAME"
fi

if test -f "/secrets/oracle/user/password"; then
  export DATASOURCE_PASSWORD=$(cat /secrets/oracle/user/password)
  echo "Eksporterer variabel DATASOURCE_PASSWORD"
fi


export JAVA_OPTS="$JAVA_OPTS -Dno.nav.modig.security.sts.url=$SECURITY_TOKEN_SERVICE_URL -Dno.nav.modig.security.systemuser.username=$SERVICEUSER_USERNAME -Dno.nav.modig.security.systemuser.password=$SERVICEUSER_PASSWORD -Dopenam.restUrl=$OPENAM_REST_URL -Dopenam.url=$OPENAM_REST_URL -Darena.personInfoService.username=$PERSONINFORMASJON_USERNAME -Darena.personInfoService.password=$PERSONINFORMASJON_PASSWORD -Dinnsending.username=$INNSENDING_USERNAME -Dinnsending.password=$INNSENDING_PASSWORD"
