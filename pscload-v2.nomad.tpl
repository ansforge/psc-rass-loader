job "pscload" {
  datacenters = ["${datacenter}"]
  type = "service"
  namespace = "${nomad_namespace}"
  
  vault {
    policies = ["psc-ecosystem"]
    change_mode = "restart"
  }

  group "pscload-services" {
    count = "1"
    restart {
      attempts = 3
      delay = "60s"
      interval = "1h"
      mode = "fail"
    }

    constraint {
      attribute = "$\u007Bnode.class\u007D"
      value     = "data"
    }

    network {
      port "http" {
        to = 8080
      }
    }

    task "pscload" {
      kill_timeout = "90s"
      kill_signal = "SIGTERM"
      driver = "docker"
      config {
        extra_hosts = [ "psc-api-maj.internal:$\u007BNOMAD_IP_http\u007D" ]
        image = "${artifact.image}:${artifact.tag}"
        volumes = [
          "name=${nomad_namespace}-pscload-data,io_priority=high,size=10,repl=2:/app/files-repo"
        ]
        volume_driver = "pxd"
        ports = ["http"]
      }
      template {
        data = <<EOH
{{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.certificate }}{{ end }}
EOH
        destination = "secrets/certificate.pem"
      }
      template {
        data = <<EOH
{{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.private_key }}{{ end }}
EOH
        destination = "secrets/key.pem"
      }
      template {
        data = <<EOH
{{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.cacerts }}{{ end }}
EOH
        destination = "secrets/cacerts.pem"
      }
      template {
        destination = "local/file.env"
        env = true
        data = <<EOH
PUBLIC_HOSTNAME={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.public_hostname }}{{ end }}
JAVA_TOOL_OPTIONS="-Xms1g -Xmx11g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/files-repo/ -Dspring.config.location=/secrets/application.properties -Dhttps.proxyHost=${proxy_host} -Dhttps.proxyPort=${proxy_port} -Dhttps.nonProxyHosts=${non_proxy_hosts} -Dkryo.unsafe=false -Dlogging.level.root=${log_level}"
EOH
      }
      template {
        data = <<EOF
server.servlet.context-path=/pscload/v2
api.base.url=http://{{ range service "${nomad_namespace}-psc-api-maj-v2" }}{{ .Address }}:{{ .Port }}{{ end }}/psc-api-maj/api
pscextract.base.url=http://{{ range service "${nomad_namespace}-pscextract" }}{{ .Address }}:{{ .Port }}{{ end }}/pscextract/v1
files.directory=/app/files-repo
cert.path=/secrets/certificate.pem
key.path=/secrets/key.pem
ca.path=/secrets/cacerts.pem
{{ range service "${nomad_namespace}-psc-rabbitmq" }}
spring.rabbitmq.host={{ .Address }}
spring.rabbitmq.port={{ .Port }}{{ end }}
spring.rabbitmq.username={{ with secret "psc-ecosystem/${nomad_namespace}/rabbitmq" }}{{ .Data.data.user }}
spring.rabbitmq.password={{ .Data.data.password }}{{ end }}
extract.download.url={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.extract_download_url }}{{ end }}
test.download.url={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.test_download_url }}{{ end }}
use.x509.auth=true
keystore.password={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.keystore_password }}{{ end }}
enable.scheduler={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.enable_scheduler }}{{ end }}
schedule.cron.expression = 0 0 12,15,18,21 * * ?
schedule.cron.timeZone = Europe/Paris
process.expiration.delay=12
management.endpoints.web.exposure.include=health,info,prometheus,metric
spring.servlet.multipart.max-file-size=600MB
spring.servlet.multipart.max-request-size=600MB
deactivation.excluded.profession.codes={{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}{{ .Data.data.deactivation_codes_exclusion_list }}{{ end }}
spring.mail.host={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_server_host }}{{ end }}
spring.mail.port={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_server_port }}{{ end }}
spring.mail.username={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_username }}{{ end }}
spring.mail.password={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_password }}{{ end }}
spring.mail.properties.mail.smtp.auth={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_smtp_auth }}{{ end }}
spring.mail.properties.mail.smtp.starttls.enable={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_enable_tls }}{{ end }}
secpsc.environment={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.platform }}{{ end }}
pscload.mail.receiver={{ with secret "psc-ecosystem/${nomad_namespace}/admin" }}{{ .Data.data.mail_receiver }}{{ end }}
enable.emailing=true
{{ with secret "psc-ecosystem/${nomad_namespace}/pscload" }}snitch={{ .Data.data.debug }}{{ end }}
EOF
        destination = "secrets/application.properties"
        change_mode = "restart"
      }
      resources {
        cpu = 300
        memory = 13312
      }
      service {
        name = "$\u007BNOMAD_NAMESPACE\u007D-$\u007BNOMAD_JOB_NAME\u007D"
        tags = ["urlprefix-$\u007BPUBLIC_HOSTNAME\u007D/pscload/v2/"]
        port = "http"
        check {
          type = "http"
          path = "/pscload/v2/check"
          port = "http"
          interval = "30s"
          timeout = "2s"
          failures_before_critical = 5
        }
      }
    }

    task "log-shipper" {
      driver = "docker"
      restart {
        interval = "30m"
        attempts = 5
        delay    = "15s"
        mode     = "delay"
      }
      meta {
        INSTANCE = "$\u007BNOMAD_ALLOC_NAME\u007D"
      }
      template {
        data = <<EOH
LOGSTASH_HOST = {{ range service "${nomad_namespace}-logstash" }}{{ .Address }}:{{ .Port }}{{ end }}
ENVIRONMENT = "${datacenter}"
EOH
        destination = "local/file.env"
        env = true
      }
      config {
        image = "${registry_path}/filebeat:7.14.2"
      }
    }
  }
}

