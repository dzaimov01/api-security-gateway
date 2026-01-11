package com.acme.apigateway.security;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpCidrMatcher {
  private final BigInteger start;
  private final BigInteger end;

  public IpCidrMatcher(final String cidr) {
    String[] parts = cidr.split("/");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid CIDR: " + cidr);
    }
    InetAddress address = parse(parts[0]);
    int prefixLength = Integer.parseInt(parts[1]);
    int totalBits = address.getAddress().length * 8;
    BigInteger ip = new BigInteger(1, address.getAddress());
    BigInteger mask = BigInteger.ONE.shiftLeft(totalBits).subtract(BigInteger.ONE)
        .shiftRight(prefixLength)
        .not()
        .and(BigInteger.ONE.shiftLeft(totalBits).subtract(BigInteger.ONE));
    this.start = ip.and(mask);
    this.end = start.add(BigInteger.ONE.shiftLeft(totalBits - prefixLength).subtract(BigInteger.ONE));
  }

  public boolean matches(final String ip) {
    InetAddress address = parse(ip);
    BigInteger value = new BigInteger(1, address.getAddress());
    return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
  }

  private InetAddress parse(final String value) {
    try {
      return InetAddress.getByName(value);
    } catch (UnknownHostException e) {
      throw new IllegalArgumentException("Invalid IP: " + value, e);
    }
  }
}
