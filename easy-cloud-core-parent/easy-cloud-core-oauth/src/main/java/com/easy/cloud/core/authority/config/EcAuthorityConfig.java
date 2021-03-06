package com.easy.cloud.core.authority.config;

import com.easy.cloud.core.authority.realm.EcAuthorityRealm;
import com.easy.cloud.core.operator.sysfilterconfig.service.EcSysFilterConfigService;
import com.easy.cloud.core.operator.sysfilterconfig.service.impl.EcSysFilterConfigJdbcServiceImpl;
import com.easy.cloud.core.operator.sysfilterconfig.service.impl.EcSysFilterConfigMemoryServiceImpl;
import com.easy.cloud.core.operator.sysfilterconfig.service.impl.EcSysFilterConfigRedisServiceImpl;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;

/**
 * <p>
 * 权限配置类
 * </p>
 *
 * @author daiqi
 * @创建时间 2018年5月25日 下午2:15:56
 */
@Configuration
@PropertySource({"classpath:config/redis-default.properties"})
public class EcAuthorityConfig {
    @Value("${ec.authority.md5.hashIterations}")
    private int hashIterations;
    @Value("classpath:config/shiro-filter.yml")
    private Resource shiroConfig;

    @Bean
    @ConditionalOnProperty(value = "ec.oauth.filter.config.redis")
    @ConditionalOnMissingBean
    public EcSysFilterConfigService filterConfigRedisService() {
        return new EcSysFilterConfigRedisServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(value = "ec.oauth.filter.config.jdbc")
    @ConditionalOnMissingBean
    public EcSysFilterConfigService filterConfigJdbcService() {
        return new EcSysFilterConfigJdbcServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public EcSysFilterConfigService filterConfigMemoryService() {
        return new EcSysFilterConfigMemoryServiceImpl().buildShiroConfig(shiroConfig);
    }

    /**
     * 将配置文件的属性设置到ShiroFilterFactoryBean对象中
     */
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        Yaml yaml = new Yaml();
        ShiroFilterFactoryBean shiroFilterFactoryBean;
        try {
            shiroFilterFactoryBean = yaml.loadAs(shiroConfig.getInputStream(), ShiroFilterFactoryBean.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return shiroFilterFactoryBean;
    }

    @Bean
    @ConditionalOnMissingBean
    protected EcBaseAuthorityCustomFilterConfig customFilterConfig() {
        return new EcBaseAuthorityCustomFilterConfig();
    }

    /**
     * <p>
     * shiro过滤器工厂bean
     * </p>
     *
     * @param securityManager
     * @return org.apache.shiro.spring.web.ShiroFilterFactoryBean
     * @author daiqi
     * @date 2018/6/27 10:39
     */
    @Bean
    @Order(value = 0)
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager, EcBaseAuthorityCustomFilterConfig customFilterConfig, EcSysFilterConfigService filterConfigService) throws Exception {
        ShiroFilterFactoryBean shiroFilterFactoryBean = shiroFilterFactoryBean();
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterConfigService.loadFilterChainDefinitions());
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.getFilters().putAll(customFilterConfig.customFilters());
        return shiroFilterFactoryBean;
    }


    /**
     * 凭证匹配器 （由于我们的密码校验交给Shiro的SimpleAuthenticationInfo进行处理了 ）
     *
     * @return
     */
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        // 散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashAlgorithmName("MD5");
        // 散列的次数，比如散列两次，相当于md5(md5(""));
        hashedCredentialsMatcher.setHashIterations(hashIterations);
        return hashedCredentialsMatcher;
    }

    @Bean
    public EcAuthorityRealm authorityRealm() {
        EcAuthorityRealm authorityRealm = new EcAuthorityRealm();
        authorityRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return authorityRealm;
    }

    /**
     * RedisSessionDAO shiro sessionDao层的实现 通过redis
     * <p>
     * 使用的是shiro-redis开源插件
     */
    @Bean
    public SessionDAO redisSessionDAO(RedisManager redisManager) {
        RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
        redisSessionDAO.setRedisManager(redisManager);
        return redisSessionDAO;
    }

    /**
     * 开启shiro aop注解支持. 使用代理方式;所以需要开启代码支持;
     *
     * @param securityManager
     * @return
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    public int getHashIterations() {
        return hashIterations;
    }
}
