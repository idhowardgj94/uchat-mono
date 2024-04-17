from strategicblue/cljs-builder as build
COPY . /usr/src

WORKDIR /usr/src
RUN rm -rf /root/.gitlibs
RUN npm install
RUN npm run prod
RUN  NODE_ENV=production npm run tw

FROM bitnami/nginx
COPY --from=build /usr/src/frontend/resources/public /app
COPY ./scripts/frontend/my_server_block.conf /opt/bitnami/nginx/conf/server_blocks/my_server_block.conf

