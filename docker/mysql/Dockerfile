# Pull base image.
FROM mysql:5.7

ADD mysqludf-redis.tar.gz /

#
COPY mysqld.cnf /etc/mysql/mysql.conf.d/
# COPY setup.sh /mysql/setup.sh
# COPY schema.sql /mysql/schema.sql

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo 'Asia/Shanghai' >/etc/timezone

# ENV MYSQL_ALLOW_EMPTY_PASSWORD yes
# CMD ["sh", "/mysql/setup.sh"]