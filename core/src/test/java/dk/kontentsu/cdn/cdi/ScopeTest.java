package dk.kontentsu.cdn.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jens Borch Christiansen
 */
public class ScopeTest {

    private static WeldContainer container;
    private static Weld weld;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        weld = new Weld();
        container = weld.initialize();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        weld.shutdown();
    }

    @Test
    public void testScope() throws Exception {
        BeanManager beanManager = CDI.current().getBeanManager();
        //Context context = beanManager.getContext(ContentScoped.class);
        /*AnnotatedType<ContentScoped> annotatedType = beanManager.createAnnotatedType(ContentScoped.class);
        BeanAttributes<ContentScoped> beanAttributes = beanManager.createBeanAttributes(annotatedType);
        beanManager.createBean(beanAttributes, ContentScoped.class, new InjectionTargetFactory<ContentScoped>() {
            @Override
            public InjectionTarget createInjectionTarget(final Bean bean) {
                return new WrappingInjectionTarget(beanManager.getInjectionTargetFactory(annotatedType).createInjectionTarget(bean));
            }

        });*/
    }

}
