package org.jed2k.protocol;

import java.util.Arrays;
import java.util.Iterator;

import org.jed2k.Utils;

public class BitField implements Iterable<Boolean> {
    byte[]  m_bytes;
    int m_size = 0;
    
    private int bitsToBytes(int count) {
        return Utils.divCeil(count, 8);
    }
    
    BitField() {
        
    }
        
    BitField(int bits) {
        resize(bits);
    }
        
    BitField(int bits, boolean val) {
        resize(bits, val); 
    }
    
    BitField(byte[] b, int bits) {
        assign(b, bits); 
    }
    
    BitField(BitField rhs) { 
        assign(rhs.bytes(), rhs.size()); 
    }

    void assign(byte[] b, int bits) { 
        resize(bits);
        System.arraycopy(b, 0, m_bytes, 0, bitsToBytes(bits)); 
        clear_trailing_bits(); 
    }

    public boolean getBit(int index)
    {
        assert(index >= 0);
        assert(index < m_size);
        return (m_bytes[index / 8] & (0x80 >> (index & 7))) != 0;
    }
        
    public void clearBit(int index)
    {
        assert(m_bytes != null);
        assert(index >= 0);
        assert(index < m_size);
        m_bytes[index / 8] &= ~(0x80 >> (index & 7));
    }

    public void setBit(int index)
    {
        assert(m_bytes != null);
        assert(index >= 0);
        assert(index < m_size);
        m_bytes[index / 8] |= (0x80 >> (index & 7));
    }

    public int size() {
        return m_size; 
    }
    
    public boolean empty() { 
        return m_size == 0; 
    }

    public byte[] bytes() { 
        assert(m_bytes != null);
        return m_bytes; 
    }

    public int count() {
        assert(m_bytes != null);
        // 0000, 0001, 0010, 0011, 0100, 0101, 0110, 0111,
        // 1000, 1001, 1010, 1011, 1100, 1101, 1110, 1111
        byte[] num_bits = {
            0, 1, 1, 2, 1, 2, 2, 3,
            1, 2, 2, 3, 2, 3, 3, 4
        };

        int ret = 0;
        int num_bytes = m_size / 8;
        for (int i = 0; i < num_bytes; ++i)
        {
            ret += num_bits[m_bytes[i] & 0xf] + num_bits[m_bytes[i] >> 4];
        }

        int rest = m_size - num_bytes * 8;
        for (int i = 0; i < rest; ++i)
        {
            ret += (m_bytes[num_bytes] >> (7-i)) & 1;
        }
        
        assert(ret <= m_size);
        assert(ret >= 0);
        return ret;
    }
    
    @Override
    public Iterator<Boolean> iterator() {
        Iterator<Boolean> it = new Iterator<Boolean>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < size();
            }

            @Override
            public Boolean next() {
                return getBit(currentIndex++);
            }

            @Override
            public void remove() {
                assert(false);
            }
        };
        
        return it;
    }

    public void resize(int bits, boolean val) {
        int s = m_size;
        int b = m_size & 7;
        resize(bits);
        
        if (s >= m_size) return;
        int old_size_bytes = bitsToBytes(s);
        int new_size_bytes = bitsToBytes(m_size);
        
        if (val) {
            if (old_size_bytes != 0 && b != 0) m_bytes[old_size_bytes - 1] |= (0xff >> b);
            if (old_size_bytes < new_size_bytes)
                Arrays.fill(m_bytes, old_size_bytes, new_size_bytes - old_size_bytes, (byte)0xff);                
            clear_trailing_bits();
        }
        else
        {
            if (old_size_bytes < new_size_bytes)
                Arrays.fill(m_bytes, old_size_bytes, new_size_bytes - old_size_bytes, (byte)0x00);
        }
    }

    void set_all() {
        Arrays.fill(m_bytes, (byte)0xff);
        clear_trailing_bits();
    }

    void clear_all() {
        Arrays.fill(m_bytes, (byte)0);
    }

    void resize(int bits)
    {
        assert(bits >= 0);
        int b = bitsToBytes(bits);
        
        byte[] new_bytes = new byte[b];
        if (m_bytes != null) {
            System.arraycopy(m_bytes, 0, new_bytes, 0, Math.min(m_bytes.length, new_bytes.length));
        }
        
        m_size = bits;
        clear_trailing_bits();
    }

    private void clear_trailing_bits() {
        // clear the tail bits in the last byte
        if ((m_size & 7) != 0) m_bytes[bitsToBytes(m_size) - 1] &= 0xff << (8 - (m_size & 7));
    }
}