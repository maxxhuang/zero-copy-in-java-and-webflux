#!/bin/bash
#
# The dot command (. or source) evaluates the script in your current shell. Executing the script first spawns a subshell
# Include globally variables
#source /opt/ruckuswireless/wsg/conf/collectd/user_defined.properties

source /set_variable.sh

EXECUTE_INTERVAL=$monitor_interval

# Local Variable
ps_grep_keyword=$app_name
ps_name="${ps_grep_keyword// /-}"
ps_id=
NEXT_PS_EXECUTE_INTERVAL=300
TIMER=$NEXT_PS_EXECUTE_INTERVAL

# Local Variable for IO rates
ps_id_old=
time_old=
io_read_old=
io_write_old=

find_pid () {
    filter_for_cacti="tail -F" # A workaround for compatible to Cacti in SZ35, Cacti use "tail -F" command to report data. So that, filter it.

    ps_id_old=$ps_id
    ps_id=`ps -ef | grep "$ps_grep_keyword" | grep -v grep | grep -v monitor_ps | awk '{print $2}'| sort -n -r | awk 'FNR==1'`
    reset_iorate_timer_if_pid_changed
    TIMER=0
}

countUpTimer () {
    TIMER=`expr $TIMER + $EXECUTE_INTERVAL`
}

reset_iorate_timer_if_pid_changed () {
    if [ "$ps_id_old" != "$ps_id" ]; then
        time_old=
    fi
}

while true; do
    now=$(date +%s)

    if [ "$TIMER" -ge "$NEXT_PS_EXECUTE_INTERVAL" ]; then
        find_pid
    fi

    if [ -z $ps_id ]; then
        echo "PUTVAL exec/sys-tmp/gauge-not_support_ps_$ps_name $(date +%s):0"
        countUpTimer
        sleep $EXECUTE_INTERVAL
        continue
    else
        top=`top -b -p $ps_id -n2 | awk 'FNR==8' | awk '{print $9}'`
        res=`pidstat -p $ps_id -r | awk 'FNR==4' | awk '{print $7 * 1024}'`
        cpu_user=`pidstat -p $ps_id -u 1 1 | awk 'FNR==4' | awk '{print $4}'`
        cpu_system=`pidstat -p $ps_id -u 1 1 | awk 'FNR==4' | awk '{print $5}'`
        fd_number=`sudo ls "/proc/$ps_id/fd" | grep -v cannot | wc -l`
        io_read_new=`sudo cat /proc/$ps_id/io | grep read_bytes | awk {'print $2'}`
        io_write_new=`sudo cat /proc/$ps_id/io | grep write_bytes | grep -v cancelled_write_bytes | awk {'print $2'}`

        if [ -z "$time_old" ]; then
            io_read=0
            io_write=0
        else
            elapsed_time=`expr $now - $time_old`
            io_read_delta=`expr $io_read_new - $io_read_old`
            io_write_delta=`expr $io_write_new - $io_write_old`
            if [ "$io_read_delta" == "0" ]; then
                io_read=0
            else
                io_read=`expr $io_read_delta / $elapsed_time`
            fi
            if [ "$io_write_delta" == "0" ]; then
                io_write=0
            else
                io_write=`expr $io_write_delta / $elapsed_time`
            fi
        fi

        if [ -z $top ]; then
            find_pid
            continue
        fi

        echo "PUTVAL zerocopyapp/ps/gauge-pid $now:$ps_id"
        echo "PUTVAL zerocopyapp/ps/gauge-top $now:$top"
        echo "PUTVAL zerocopyapp/ps/gauge-res $now:$res"
        echo "PUTVAL zerocopyapp/ps/gauge-cpu_user $now:$cpu_user"
        echo "PUTVAL zerocopyapp/ps/gauge-cpu_system $now:$cpu_system"
        echo "PUTVAL zerocopyapp/ps/gauge-fd_number $now:$fd_number"
        echo "PUTVAL zerocopyapp/ps/gauge-io_read $now:$io_read"
        echo "PUTVAL zerocopyapp/ps/gauge-io_write $now:$io_write"

        time_old=$now
        io_read_old=$io_read_new
        io_write_old=$io_write_new
    fi

    countUpTimer
    sleep $EXECUTE_INTERVAL

done
