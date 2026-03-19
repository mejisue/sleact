package mejisue.sleact.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthFilter extends GenericFilterBean {

    @Value("${jwt.secretKey}")
    private String secretKey;

    private static final String[] PERMIT_ALL_PATHS =
            {"/api/users/signup", "/api/users/login"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // 1. PermitAll 경로인 경우 필터 체인을 계속 진행하고 즉시 종료
        for (String path : PERMIT_ALL_PATHS) {
            // 토큰 검증 없이 바로 통과
            if (requestURI.equals(path)) {
                filterChain.doFilter(request, response);
                return; // 필터 체인 종료
            }
        }

        String token = httpRequest.getHeader("Authorization");
        try{
            if(token != null){

                if(!token.substring(0,7).equals("Bearer ")){
                    throw new AuthenticationException("Bearer 형식이 아닙니다.");
                }
                String jwtToken = token.substring(7);

                // 토큰 검증 및 claims 추출
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtToken)
                        .getBody();

                // Authentication 객체 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_"+claims.get("role").toString()));
                UserDetails userDetails = new User(claims.getSubject(),"",authorities);
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            filterChain.doFilter(request, response);
        }catch(Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("invalid token");
        }
    }
}
