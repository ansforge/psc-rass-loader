project = "prosanteconnect/${workspace.name}/pscload-v2"

# Labels can be specified for organizational purposes.
labels = {
  "domaine" = "psc"
}

runner {
  enabled = true
  profile = "secpsc-${workspace.name}"
  data_source "git" {
    url = "https://github.com/prosanteconnect/pscload-v2.git"
    ref = "${workspace.name}"
  }
  poll {
    enabled = false
  }
}

# An application to deploy.
app "prosanteconnect/pscload-v2" {
  # Build specifies how an application should be deployed. In this case,
  # we'll build using a Dockerfile and keeping it in a local registry.
  build {
    use "docker" {
      build_args = {"PROSANTECONNECT_PACKAGE_GITHUB_TOKEN"="${var.github_token}"}
      disable_entrypoint = true
      build_args = {"PROSANTECONNECT_PACKAGE_GITHUB_TOKEN"="${var.github_token}"}
    }
    # Uncomment below to use a remote docker registry to push your built images.
    registry {
      use "docker" {
        image = "${var.registry_username}/pscload-v2"
        tag = gitrefpretty()
        username = var.registry_username
        password = var.registry_password
        local = var.is_local_registry
      }
    }
  }

  # Deploy to Nomad
  deploy {
    use "nomad-jobspec" {
      jobspec = templatefile("${path.app}/pscload-v2.nomad.tpl", {
        datacenter = var.datacenter
        nomad_namespace = var.nomad_namespace
        log_level = var.log_level
        registry_path = var.registry_username
        disable_messages = var.disable_messages
      })
    }
  }
}

variable "datacenter" {
  type = string
  default = ""
  env = ["NOMAD_DATACENTER"]
}

variable "nomad_namespace" {
  type = string
  default = ""
  env = ["NOMAD_NAMESPACE"]
}

variable "is_local_registry" {
  type = bool
  default = true
  env = ["LOCAL_REGISTRY"]
}

variable "registry_username" {
  type    = string
  default = ""
  env     = ["REGISTRY_USERNAME"]
  sensitive = true
}

variable "registry_password" {
  type    = string
  default = ""
  env     = ["REGISTRY_PASSWORD"]
  sensitive = true
}

variable "log_level" {
  type = string
  default = "INFO"
}

variable "disable_messages" {
  type = string
  default = "false"
}

variable "github_token" {
  type    = string
  default = ""
  env     = ["PROSANTECONNECT_PACKAGE_GITHUB_TOKEN"]
  sensitive = true
}
