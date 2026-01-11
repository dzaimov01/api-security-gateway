CREATE TABLE routes (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  path_pattern VARCHAR(512) NOT NULL,
  upstream_url VARCHAR(1024) NOT NULL,
  methods VARCHAR(128),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE policies (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  type VARCHAR(64) NOT NULL,
  config_json JSONB NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE policy_assignments (
  id UUID PRIMARY KEY,
  route_id UUID NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
  policy_id UUID NOT NULL REFERENCES policies(id) ON DELETE CASCADE,
  order_index INT NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE ip_rules (
  id UUID PRIMARY KEY,
  scope VARCHAR(64) NOT NULL,
  cidr VARCHAR(64) NOT NULL,
  action VARCHAR(16) NOT NULL,
  route_id UUID REFERENCES routes(id),
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE api_clients (
  id UUID PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  api_key_hash VARCHAR(128) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  request_id UUID NOT NULL,
  client_ip VARCHAR(64) NOT NULL,
  route_id UUID,
  route_pattern VARCHAR(512),
  decision VARCHAR(16) NOT NULL,
  details_json JSONB,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_routes_path_pattern ON routes(path_pattern);
CREATE INDEX idx_policies_type ON policies(type);
CREATE INDEX idx_policy_assignments_route ON policy_assignments(route_id, order_index);
CREATE INDEX idx_ip_rules_route ON ip_rules(route_id, action);
CREATE INDEX idx_audit_logs_request_id ON audit_logs(request_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
