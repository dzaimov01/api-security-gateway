package com.acme.apigateway.admin;

import com.acme.apigateway.admin.dto.ApiClientRequest;
import com.acme.apigateway.admin.dto.AssignmentRequest;
import com.acme.apigateway.admin.dto.IpRuleRequest;
import com.acme.apigateway.admin.dto.PolicyRequest;
import com.acme.apigateway.admin.dto.RouteRequest;
import com.acme.apigateway.infra.db.entity.ApiClientEntity;
import com.acme.apigateway.infra.db.entity.IpRuleEntity;
import com.acme.apigateway.infra.db.entity.PolicyAssignmentEntity;
import com.acme.apigateway.infra.db.entity.PolicyEntity;
import com.acme.apigateway.infra.db.entity.RouteEntity;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/admin")
public class AdminController {
  private final AdminService adminService;

  public AdminController(final AdminService adminService) {
    this.adminService = adminService;
  }

  @GetMapping("/routes")
  public Flux<RouteEntity> listRoutes() {
    return adminService.listRoutes();
  }

  @PostMapping("/routes")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<RouteEntity> createRoute(@Valid @RequestBody final RouteRequest request) {
    return adminService.createRoute(request);
  }

  @PutMapping("/routes/{id}")
  public Mono<RouteEntity> updateRoute(@PathVariable final UUID id, @Valid @RequestBody final RouteRequest request) {
    return adminService.updateRoute(id, request);
  }

  @DeleteMapping("/routes/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteRoute(@PathVariable final UUID id) {
    return adminService.deleteRoute(id);
  }

  @GetMapping("/policies")
  public Flux<PolicyEntity> listPolicies() {
    return adminService.listPolicies();
  }

  @PostMapping("/policies")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<PolicyEntity> createPolicy(@Valid @RequestBody final PolicyRequest request) {
    return adminService.createPolicy(request);
  }

  @GetMapping("/routes/{id}/assignments")
  public Flux<PolicyAssignmentEntity> listAssignments(@PathVariable final UUID id) {
    return adminService.listAssignments(id);
  }

  @PostMapping("/routes/{id}/assignments")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> assignPolicies(@PathVariable final UUID id, @Valid @RequestBody final AssignmentRequest request) {
    return adminService.assignPolicies(id, request);
  }

  @GetMapping("/ip-rules")
  public Flux<IpRuleEntity> listIpRules() {
    return adminService.listIpRules();
  }

  @PostMapping("/ip-rules")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<IpRuleEntity> createIpRule(@Valid @RequestBody final IpRuleRequest request) {
    return adminService.createIpRule(request);
  }

  @GetMapping("/api-clients")
  public Flux<ApiClientEntity> listApiClients() {
    return adminService.listApiClients();
  }

  @PostMapping("/api-clients")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<ApiClientEntity> createApiClient(@Valid @RequestBody final ApiClientRequest request) {
    return adminService.createApiClient(request);
  }
}
