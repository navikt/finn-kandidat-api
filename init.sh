if test -f "/secrets/serviceuser/username"; then
  export SERVICEUSER_USERNAME=$(cat /secret/serviceuser/username)
  echo "Eksporterer variabel SERVICEUSER_USERNAME"
else
  echo "Eksporterer IKKE variabel SERVICEUSER_USERNAME fordi filen /secrets/serviceuser/username ikke finnes"
fi

if test -f "/secrets/serviceuser/password"; then
  export SERVICEUSER_PASSWORD=$(cat /secret/serviceuser/password)
  echo "Eksporterer variabel SERVICEUSER_PASSWORD"
else
  echo "Eksporterer IKKE variabel SERVICEUSER_PASSWORD fordi filen /secrets/serviceuser/username ikke finnes"
fi
