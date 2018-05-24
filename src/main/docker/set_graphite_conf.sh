#! /bin/bash

#sed -i "s/^\(\s*Host \s*\"\s*\).*$/\1$graphite_ip\"/" $graphite_conf
#sed -i "s/^\(\s*Port \s*\"\s*\).*$/\1$graphite_port\"/" $graphite_conf

graphite_conf=/etc/collectd.d/write_graphite.conf

#sed -i -e 's/^[[:space:]]*Host[[:space:]]([[:digit:]]{1,3}\.[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+)".*$/\1$graphite_ip' /tmp/g.conf
sed -i -e "s/^\([[:space:]]*Host[[:space:]]*\).*$/\1\"$graphite_ip\"/" $graphite_conf
sed -i -e "s/^\([[:space:]]*Port[[:space:]]*\).*$/\1\"$graphite_port\"/" $graphite_conf

dot="."
metrics_prefix=$es_node_name$dot
sed -i -e "s/^\([[:space:]]*Prefix [[:space:]]*\"[[:space:]]*\).*$/\1$metrics_prefix\"/" $graphite_conf

echo interpolated $graphite_conf:
cat $graphite_conf