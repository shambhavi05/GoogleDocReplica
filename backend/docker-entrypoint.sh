#!/bin/sh
# Render's managed-Postgres "Internal Connection String" looks like:
#   postgresql://user:pass@host:port/dbname
# The JDBC driver needs the jdbc:postgresql:// scheme, and does NOT accept
# user:pass embedded in the URL the way a plain Postgres URI does — those must
# be passed separately as DB_USERNAME/DB_PASSWORD. This splits one into the other
# so the same image runs locally (DB_URL/DB_USERNAME/DB_PASSWORD already set) and
# on Render (only DATABASE_URL is set, in the combined form).
if [ -n "$DATABASE_URL" ]; then
  # Strip scheme, then split userinfo@hostpart
  rest="${DATABASE_URL#postgresql://}"
  rest="${rest#postgres://}"
  userinfo="${rest%%@*}"
  hostpart="${rest#*@}"

  export DB_USERNAME="${userinfo%%:*}"
  export DB_PASSWORD="${userinfo#*:}"
  export DB_URL="jdbc:postgresql://${hostpart}"
fi

exec java -jar app.jar
