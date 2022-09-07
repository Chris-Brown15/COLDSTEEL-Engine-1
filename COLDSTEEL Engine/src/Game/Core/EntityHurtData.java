package Game.Core;


/*
 * Utility record used for delivering data to an entity who has just be hurt, this is subequently passed to kill as well
 */
public record EntityHurtData (DamageType damageType , float x , float y) {}
