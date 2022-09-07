'''
convienience wrapper over the projectile scripting interface for scripting projectile objects
'''

def projectileScan(radius):
	pLib.scanForEntities(P , radius , onHit)
