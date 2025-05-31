package press.mizhifei.dentist.auth.annotation;

import press.mizhifei.dentist.auth.model.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for role-based access control.
 * 
 * This annotation can be applied to controller methods to enforce role-based access control.
 * It supports both single role and multiple roles syntax, using logical OR (user needs ANY of the specified roles).
 * 
 * Usage examples:
 * - Single role: @RequireRoles(Role.SYSTEM_ADMIN)
 * - Multiple roles: @RequireRoles({Role.SYSTEM_ADMIN, Role.CLINIC_ADMIN})
 * 
 * The annotation integrates with the existing JWT authentication system and validates user roles
 * before method execution using AOP.
 * 
 * @author zhifeimi
 * @email zm377@uowmail.edu.au
 * @github https://github.com/zm377
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRoles {
    
    /**
     * The roles required to access the annotated method.
     * User must have at least one of the specified roles (logical OR).
     * 
     * @return array of required roles
     */
    Role[] value();
}
