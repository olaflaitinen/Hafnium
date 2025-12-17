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
Service labels
*/}}
{{- define "hafnium-backend.serviceLabels" -}}
app: {{ .serviceName }}
tier: backend
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "hafnium-backend.serviceAccountName" -}}
{{- default "hafnium-service" .Values.serviceAccount.name }}
{{- end }}

{{/*
Database JDBC URL
*/}}
{{- define "hafnium-backend.jdbcUrl" -}}
jdbc:postgresql://{{ .Values.database.host }}:{{ .Values.database.port }}/{{ .Values.database.name }}
{{- end }}
