package org.jboss.forge.container;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.jboss.forge.container.event.PostStartup;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Singleton
public class ContainerLifecycleEventObserver
{
   private boolean observedPerform;

   public void perform(@Observes PostStartup event)
   {
      this.observedPerform = true;
   }

   public boolean isObservedPerform()
   {
      return observedPerform;
   }
}
