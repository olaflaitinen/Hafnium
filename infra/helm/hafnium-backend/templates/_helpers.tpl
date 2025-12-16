{{/*
Expand the name of the chart.
*/}}
{{- define "hafnium-backend.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "hafnium-backend.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "hafnium-backend.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "hafnium-backend.labels" -}}
helm.sh/chart: {{ include "hafnium-backend.chart" . }}
{{ include "hafnium-backend.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "hafnium-backend.selectorLabels" -}}
app.kubernetes.io/name: {{ include "hafnium-backend.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "hafnium-backend.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "hafnium-backend.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Common environment variables
*/}}
{{- define "hafnium-backend.commonEnv" -}}
- name: DB_HOST
  value: {{ .Values.config.database.host | quote }}
- name: DB_PORT
  value: {{ .Values.config.database.port | quote }}
- name: DB_NAME
  value: {{ .Values.config.database.name | quote }}
- name: KAFKA_BOOTSTRAP_SERVERS
  value: {{ .Values.config.kafka.bootstrapServers | quote }}
- name: REDIS_HOST
  value: {{ .Values.config.redis.host | quote }}
- name: REDIS_PORT
  value: {{ .Values.config.redis.port | quote }}
- name: KEYCLOAK_ISSUER_URI
  value: {{ .Values.config.keycloak.issuerUri | quote }}
- name: OPA_URL
  value: {{ .Values.config.opa.url | quote }}
{{- end }}
