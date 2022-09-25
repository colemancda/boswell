package network.packet

import net.server.channel.handlers.SummonDamageHandler.SummonAttackEntry
import network.opcode.SendOpcode
import server.maps.MapleSummon
import server.movement.LifeMovementFragment
import tools.data.output.LittleEndianWriter
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point

class SummonedPool {

    companion object Packet {
        /**
         * Gets a packet to spawn a special map object.
         *
         * @param summon
         * @param skillLevel The level of the skill used.
         * @param animated Animated spawn?
         * @return The spawn packet for the map object.
         */
        fun onSummonCreated(summon: MapleSummon, animated: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(25)
            mplew.writeShort(SendOpcode.SPAWN_SPECIAL_MAPOBJECT.value)
            mplew.writeInt(summon.owner.id)
            mplew.writeInt(summon.objectId)
            mplew.writeInt(summon.skill)
            mplew.write(0x0A) //v83
            mplew.write(summon.skillLevel)
            mplew.writePos(summon.position)
            mplew.write(summon.stance) //bMoveAction & foothold, found thanks to Rien dev team
            //mplew.writeShort(0);
            mplew.writeShort(summon.footHold) //foothold
            mplew.write(summon.movementType.value) // 0 = don't move, 1 = follow (4th mage summons?), 2/4 = only tele follow, 3 = bird follow
            mplew.write(if (summon.isPuppet) 0 else 1) // 0 and the summon can't attack - but puppets don't attack with 1 either ^.-
            mplew.write(if (animated) 0 else 1)

            return mplew.packet
        }

        /**
         * Gets a packet to remove a special map object.
         *
         * @param summon
         * @param animated Animated removal?
         * @return The packet removing the object.
         */
        fun onSummonRemoved(summon: MapleSummon, animated: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(11)
            mplew.writeShort(SendOpcode.REMOVE_SPECIAL_MAPOBJECT.value)
            mplew.writeInt(summon.owner.id)
            mplew.writeInt(summon.objectId)
            mplew.write(if (animated) 4 else 1) // ?

            return mplew.packet
        }

        fun moveSummon(cid: Int, oid: Int, startPos: Point?, moves: List<LifeMovementFragment?>?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MOVE_SUMMON.value)
            mplew.writeInt(cid)
            mplew.writeInt(oid)
            mplew.writePos(startPos)
            serializeMovementList(mplew, moves)

            return mplew.packet
        }

        fun summonAttack(cid: Int, summonOid: Int, direction: Byte, allDamage: List<SummonAttackEntry>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            //b2 00 29 f7 00 00 9a a3 04 00 c8 04 01 94 a3 04 00 06 ff 2b 00
            mplew.writeShort(SendOpcode.SUMMON_ATTACK.value)
            mplew.writeInt(cid)
            mplew.writeInt(summonOid)
            mplew.write(0) // char level
            mplew.write(direction)
            mplew.write(allDamage.size)
            for (attackEntry in allDamage) {
                mplew.writeInt(attackEntry.monsterOid) // oid
                mplew.write(6) // who knows
                mplew.writeInt(attackEntry.damage) // damage
            }

            return mplew.packet
        }

        fun summonDamaged(
            cid: Int,
            oid: Int,
            damage: Int,
            action: Int,
            monsterIdFrom: Int,
            isLeft: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SUMMON_DAMAGE.value)
            mplew.writeInt(cid)
            mplew.writeInt(oid)
            mplew.write(action)
            mplew.writeInt(damage)
            mplew.writeInt(monsterIdFrom)
            mplew.writeBool(isLeft)

            return mplew.packet
        }

        fun summonSkill(cid: Int, summonSkillId: Int, newStance: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SUMMON_SKILL.value)
            mplew.writeInt(cid)
            mplew.writeInt(summonSkillId)
            mplew.write(newStance)

            return mplew.packet
        }

        private fun serializeMovementList(lew: LittleEndianWriter, moves: List<LifeMovementFragment?>?) {
            if (moves != null) {
                lew.write(moves.size)
            }
            if (moves != null) {
                for (move in moves) {
                    move?.serialize(lew)
                }
            }
        }
    }
}